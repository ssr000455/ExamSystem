package render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ImageLoader {
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final int MAX_CACHE_SIZE = 50;
    
    public static Image loadImage(String path) throws IOException {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        
        BufferedImage img = ImageIO.read(new File(path));
        
        // LRU 清理
        if (imageCache.size() >= MAX_CACHE_SIZE) {
            String firstKey = imageCache.keySet().iterator().next();
            imageCache.remove(firstKey);
        }
        
        imageCache.put(path, img);
        return img;
    }
    
    public static Image loadScaledImage(String path, int width, int height) throws IOException {
        String key = path + "@" + width + "x" + height;
        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }
        
        Image img = loadImage(path);
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        imageCache.put(key, scaled);
        return scaled;
    }
    
    public static void clearCache() {
        imageCache.clear();
    }
}
