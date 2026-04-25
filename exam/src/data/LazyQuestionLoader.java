package data;

import model.*;
import java.util.*;
import java.util.function.Supplier;

public class LazyQuestionLoader {
    private static final Map<String, Supplier<List<Question>>> lazyLoaders = new HashMap<>();
    private static final Map<String, List<Question>> loadedQuestions = new HashMap<>();
    
    public static void register(String sectionId, Supplier<List<Question>> loader) {
        lazyLoaders.put(sectionId, loader);
    }
    
    public static List<Question> getQuestions(String sectionId) {
        if (loadedQuestions.containsKey(sectionId)) {
            return loadedQuestions.get(sectionId);
        }
        
        Supplier<List<Question>> loader = lazyLoaders.get(sectionId);
        if (loader != null) {
            List<Question> questions = loader.get();
            loadedQuestions.put(sectionId, questions);
            return questions;
        }
        
        return Collections.emptyList();
    }
    
    public static void preloadNext(String currentSectionId) {
        // 预加载下一个框题
        String[] parts = currentSectionId.split("/");
        if (parts.length >= 2) {
            try {
                int num = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                String nextId = parts[0] + "/section" + (num + 1);
                if (lazyLoaders.containsKey(nextId)) {
                    new Thread(() -> getQuestions(nextId)).start();
                }
            } catch (Exception ignored) {}
        }
    }
}
