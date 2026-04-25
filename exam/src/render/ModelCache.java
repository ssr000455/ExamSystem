package render;

import java.util.*;
import java.lang.ref.SoftReference;

public class ModelCache {
    private static final Map<String, SoftReference<Object>> cache = new HashMap<>();
    
    public static void put(String key, Object model) {
        cache.put(key, new SoftReference<>(model));
    }
    
    public static Object get(String key) {
        SoftReference<Object> ref = cache.get(key);
        return ref != null ? ref.get() : null;
    }
    
    public static void clear() {
        cache.clear();
    }
    
    public static int size() {
        return cache.size();
    }
}
