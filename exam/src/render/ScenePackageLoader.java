package render;

import model.SceneConfig;
import com.google.gson.*;
import javax.tools.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;

public class ScenePackageLoader {
    private static final Logger LOGGER = Logger.getLogger(ScenePackageLoader.class.getName());
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, ScenePackage> packageCache = new HashMap<>();
    
    public static class ScenePackage {
        public String packageId;
        public Path extractedPath;
        public SceneConfig config;
        public Map<String, byte[]> customModels = new HashMap<>();
        public Map<String, byte[]> customTextures = new HashMap<>();
        public Class<?> mainClass;
        public Object mainInstance;
        public Map<String, Object> renderContext = new HashMap<>();
        public List<GameObject> gameObjects = new ArrayList<>();
        public CameraInfo camera = new CameraInfo();
    }
    
    public static class GameObject {
        public String id;
        public String modelSource;
        public double[] position = {0,0,0};
        public double[] rotation = {0,0,0};
        public double[] scale = {1,1,1};
        public String textureSource;
    }
    
    public static class CameraInfo {
        public double[] position = {5,3,5};
        public double[] target = {0,0,0};
        public double[] up = {0,1,0};
    }
    
    public static ScenePackage loadPackage(String packagePath) throws Exception {
        if (packageCache.containsKey(packagePath)) {
            return packageCache.get(packagePath);
        }
        
        Path zipPath = Paths.get(packagePath);
        if (!Files.exists(zipPath)) {
            throw new FileNotFoundException("场景包不存在: " + packagePath);
        }
        
        ScenePackage pkg = new ScenePackage();
        pkg.packageId = zipPath.getFileName().toString().replace(".zip", "");
        
        Path tempDir = Files.createTempDirectory("scene_" + pkg.packageId + "_");
        tempDir.toFile().deleteOnExit();
        pkg.extractedPath = tempDir;
        
        // 解压
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path targetPath = tempDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    byte[] data = zis.readAllBytes();
                    Files.write(targetPath, data);
                    
                    String name = entry.getName().toLowerCase();
                    if (name.endsWith(".obj")) pkg.customModels.put(entry.getName(), data);
                    else if (name.endsWith(".png") || name.endsWith(".jpg")) pkg.customTextures.put(entry.getName(), data);
                }
                zis.closeEntry();
            }
        }
        
        // 读取场景配置
        Path configPath = tempDir.resolve("scene.json");
        if (Files.exists(configPath)) {
            String json = Files.readString(configPath);
            pkg.config = gson.fromJson(json, SceneConfig.class);
        }
        
        // 编译 Main.java
        Path mainJavaPath = tempDir.resolve("Main.java");
        if (Files.exists(mainJavaPath)) {
            pkg.mainClass = compileAndLoadMain(mainJavaPath, tempDir);
            if (pkg.mainClass != null) {
                pkg.mainInstance = pkg.mainClass.getDeclaredConstructor().newInstance();
                
                // 准备上下文数据
                Map<String, Object> context = new HashMap<>();
                if (pkg.config != null) {
                    context.put("camera", pkg.config.camera);
                    context.put("models", pkg.config.models);
                    context.put("environment", pkg.config.environment);
                }
                pkg.renderContext = context;
                
                // 调用 init
                try {
                    java.lang.reflect.Method initMethod = pkg.mainClass.getMethod("init", Map.class);
                    initMethod.invoke(pkg.mainInstance, context);
                } catch (NoSuchMethodException e) {}
                
                // 获取物体列表和相机
                invokeUpdateObjects(pkg);
            }
        }
        
        packageCache.put(packagePath, pkg);
        return pkg;
    }
    
    private static Class<?> compileAndLoadMain(Path javaFile, Path classPath) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            LOGGER.warning("未找到 Java 编译器，使用解释模式");
            return null;
        }
        
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        
        List<String> options = Arrays.asList("-d", classPath.toString());
        Iterable<? extends JavaFileObject> compilationUnits = 
            fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFile.toFile()));
        
        JavaCompiler.CompilationTask task = compiler.getTask(
            null, fileManager, diagnostics, options, null, compilationUnits);
        
        if (!task.call()) {
            StringBuilder sb = new StringBuilder();
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                sb.append(d.getMessage(null)).append("\n");
            }
            throw new Exception("编译失败: " + sb.toString());
        }
        
        URLClassLoader classLoader = new URLClassLoader(new URL[]{classPath.toUri().toURL()});
        return classLoader.loadClass("Main");
    }
    
    private static void invokeUpdateObjects(ScenePackage pkg) {
        if (pkg.mainInstance == null) return;
        try {
            // 获取物体列表
            java.lang.reflect.Method getObjects = pkg.mainClass.getMethod("getObjects");
            Object result = getObjects.invoke(pkg.mainInstance);
            if (result instanceof List) {
                List<?> list = (List<?>) result;
                pkg.gameObjects.clear();
                for (Object obj : list) {
                    GameObject go = convertToGameObject(obj);
                    if (go != null) pkg.gameObjects.add(go);
                }
            }
            
            // 获取相机
            java.lang.reflect.Method getCamera = pkg.mainClass.getMethod("getCamera");
            Object cam = getCamera.invoke(pkg.mainInstance);
            if (cam != null) {
                pkg.camera = convertToCameraInfo(cam);
            }
        } catch (Exception e) {
            LOGGER.warning("获取物体列表失败: " + e.getMessage());
        }
    }
    
    private static GameObject convertToGameObject(Object obj) {
        try {
            GameObject go = new GameObject();
            Class<?> clazz = obj.getClass();
            go.id = (String) clazz.getField("id").get(obj);
            go.modelSource = (String) clazz.getField("modelSource").get(obj);
            go.position = (double[]) clazz.getField("position").get(obj);
            go.rotation = (double[]) clazz.getField("rotation").get(obj);
            go.scale = (double[]) clazz.getField("scale").get(obj);
            go.textureSource = (String) clazz.getField("textureSource").get(obj);
            return go;
        } catch (Exception e) {
            return null;
        }
    }
    
    private static CameraInfo convertToCameraInfo(Object cam) {
        try {
            CameraInfo info = new CameraInfo();
            Class<?> clazz = cam.getClass();
            info.position = (double[]) clazz.getField("position").get(cam);
            info.target = (double[]) clazz.getField("target").get(cam);
            info.up = (double[]) clazz.getField("up").get(cam);
            return info;
        } catch (Exception e) {
            return new CameraInfo();
        }
    }
    
    public static void renderPackage(ScenePackage pkg, Object graphicsContext) {
        if (pkg.mainInstance == null) return;
        try {
            java.lang.reflect.Method renderMethod = pkg.mainClass.getMethod("render");
            renderMethod.invoke(pkg.mainInstance);
        } catch (Exception e) {}
    }
    
    public static void updatePackage(ScenePackage pkg, float deltaTime) {
        if (pkg.mainInstance == null) return;
        try {
            java.lang.reflect.Method updateMethod = pkg.mainClass.getMethod("update", float.class);
            updateMethod.invoke(pkg.mainInstance, deltaTime);
            invokeUpdateObjects(pkg);
        } catch (NoSuchMethodException e) {
            // 可选
        } catch (Exception e) {
            LOGGER.warning("更新场景失败: " + e.getMessage());
        }
    }
    
    public static void clearCache() {
        packageCache.clear();
    }
}
