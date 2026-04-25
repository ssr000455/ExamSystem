package data;

import model.SceneConfig;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import java.util.*;

public class SceneLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, SceneContext> sceneCache = new HashMap<>();
    
    public static class SceneContext {
        public SceneConfig config;
        public Path extractedPath;
        public Map<String, byte[]> modelData = new HashMap<>();
        public Map<String, byte[]> textureData = new HashMap<>();
    }
    
    public static SceneContext loadScene(String scenePackagePath) throws Exception {
        // 检查缓存
        if (sceneCache.containsKey(scenePackagePath)) {
            return sceneCache.get(scenePackagePath);
        }
        
        Path zipPath = Paths.get(scenePackagePath);
        if (!Files.exists(zipPath)) {
            throw new FileNotFoundException("场景包不存在: " + scenePackagePath);
        }
        
        // 创建临时解压目录
        Path tempDir = Files.createTempDirectory("scene_");
        tempDir.toFile().deleteOnExit();
        
        SceneContext context = new SceneContext();
        context.extractedPath = tempDir;
        
        // 解压 ZIP
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
                    
                    // 缓存模型和纹理数据
                    String name = entry.getName().toLowerCase();
                    if (name.endsWith(".obj") || name.endsWith(".vox")) {
                        context.modelData.put(entry.getName(), data);
                    } else if (name.endsWith(".png") || name.endsWith(".jpg")) {
                        context.textureData.put(entry.getName(), data);
                    }
                }
                zis.closeEntry();
            }
        }
        
        // 读取场景配置
        Path configPath = tempDir.resolve("scene.json");
        if (Files.exists(configPath)) {
            String json = Files.readString(configPath);
            context.config = gson.fromJson(json, SceneConfig.class);
        } else {
            throw new FileNotFoundException("场景配置文件 scene.json 不存在");
        }
        
        sceneCache.put(scenePackagePath, context);
        return context;
    }
    
    public static void clearCache() {
        sceneCache.clear();
    }
    
    public static List<String> getAvailableScenes(String scenesDir) {
        List<String> scenes = new ArrayList<>();
        Path dir = Paths.get(scenesDir);
        
        if (Files.exists(dir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.zip")) {
                for (Path entry : stream) {
                    scenes.add(entry.toString());
                }
            } catch (IOException e) {
                System.err.println("扫描场景目录失败: " + e.getMessage());
            }
        }
        
        return scenes;
    }
}
