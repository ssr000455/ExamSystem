package render;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

public class ModelLibrary {
    private static final Logger LOGGER = Logger.getLogger(ModelLibrary.class.getName());
    private static final Path MODELS_DIR = Paths.get("models");
    private static final Path BUILTIN_DIR = MODELS_DIR.resolve("builtin");
    private static final Path TEXTURES_DIR = MODELS_DIR.resolve("textures");
    
    private static final Map<String, ModelInfo> modelCache = new HashMap<>();
    private static final Map<String, TextureInfo> textureCache = new HashMap<>();
    
    public static class ModelInfo {
        public String name;
        public Path path;
        public byte[] data;
        public String textureRef;      // 默认贴图引用
        public float[] defaultScale = {1.0f, 1.0f, 1.0f};
        public float[] defaultRotation = {0.0f, 0.0f, 0.0f};
    }
    
    public static class TextureInfo {
        public String name;
        public Path path;
        public byte[] data;
        public int width;
        public int height;
    }
    
    static {
        initialize();
    }
    
    private static void initialize() {
        try {
            Files.createDirectories(BUILTIN_DIR);
            Files.createDirectories(TEXTURES_DIR);
            loadBuiltinModels();
            loadBuiltinTextures();
        } catch (IOException e) {
            LOGGER.warning("初始化模型库失败: " + e.getMessage());
        }
    }
    
    private static void loadBuiltinModels() throws IOException {
        if (!Files.exists(BUILTIN_DIR)) return;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BUILTIN_DIR, "*.obj")) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                ModelInfo info = new ModelInfo();
                info.name = name;
                info.path = path;
                info.data = Files.readAllBytes(path);
                info.textureRef = name.replace(".obj", ".png");
                modelCache.put(name, info);
                LOGGER.fine("加载内置模型: " + name);
            }
        }
    }
    
    private static void loadBuiltinTextures() throws IOException {
        if (!Files.exists(TEXTURES_DIR)) return;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEXTURES_DIR, "*.{png,jpg,jpeg}")) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                TextureInfo info = new TextureInfo();
                info.name = name;
                info.path = path;
                info.data = Files.readAllBytes(path);
                textureCache.put(name, info);
                LOGGER.fine("加载内置贴图: " + name);
            }
        }
    }
    
    public static ModelInfo getModel(String name) {
        // 先查缓存
        if (modelCache.containsKey(name)) {
            return modelCache.get(name);
        }
        
        // 尝试加载
        Path modelPath = BUILTIN_DIR.resolve(name);
        if (Files.exists(modelPath)) {
            try {
                ModelInfo info = new ModelInfo();
                info.name = name;
                info.path = modelPath;
                info.data = Files.readAllBytes(modelPath);
                info.textureRef = name.replace(".obj", ".png");
                modelCache.put(name, info);
                return info;
            } catch (IOException e) {
                LOGGER.warning("加载模型失败: " + name);
            }
        }
        
        return null;
    }
    
    public static TextureInfo getTexture(String name) {
        if (textureCache.containsKey(name)) {
            return textureCache.get(name);
        }
        
        Path texPath = TEXTURES_DIR.resolve(name);
        if (Files.exists(texPath)) {
            try {
                TextureInfo info = new TextureInfo();
                info.name = name;
                info.path = texPath;
                info.data = Files.readAllBytes(texPath);
                textureCache.put(name, info);
                return info;
            } catch (IOException e) {
                LOGGER.warning("加载贴图失败: " + name);
            }
        }
        
        return null;
    }
    
    public static List<String> listModels() {
        return new ArrayList<>(modelCache.keySet());
    }
    
    public static List<String> listTextures() {
        return new ArrayList<>(textureCache.keySet());
    }
    
    public static byte[] getDefaultTexture() {
        TextureInfo tex = getTexture("default.png");
        if (tex != null) return tex.data;
        
        // 创建默认纹理数据（简单的棋盘格）
        return createCheckerboardTexture(64, 64);
    }
    
    private static byte[] createCheckerboardTexture(int width, int height) {
        byte[] data = new byte[width * height * 4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = (y * width + x) * 4;
                boolean white = ((x / 8) + (y / 8)) % 2 == 0;
                data[idx] = (byte)(white ? 255 : 200);       // R
                data[idx + 1] = (byte)(white ? 255 : 200);   // G
                data[idx + 2] = (byte)(white ? 255 : 200);   // B
                data[idx + 3] = (byte)255;                   // A
            }
        }
        return data;
    }
}
