package progress;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

public class ProgressManager {
    private static final Path PROGRESS_FILE = Paths.get(".exam_progress.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static class ProgressData {
        public String planName;
        public String sectionName;
        public int questionIndex;
        public String currentCard = "welcome";
        public String currentDifficulty = "BEGINNER";
        public long totalStudySeconds = 0;
        public long lastSessionStart = 0;
        public Map<String, String> savedCodes = new HashMap<>();
        public Map<String, Integer> scores = new HashMap<>();
        public long lastOpened;
    }
    
    public static boolean hasProgress() {
        return Files.exists(PROGRESS_FILE);
    }
    
    public static ProgressData load() {
        if (!hasProgress()) {
            ProgressData data = new ProgressData();
            data.lastOpened = System.currentTimeMillis();
            data.lastSessionStart = data.lastOpened;
            return data;
        }
        
        try {
            String content = new String(Files.readAllBytes(PROGRESS_FILE));
            ProgressData data = gson.fromJson(content, ProgressData.class);
            if (data.currentCard == null) data.currentCard = "welcome";
            if (data.currentDifficulty == null) data.currentDifficulty = "BEGINNER";
            if (data.lastSessionStart == 0) data.lastSessionStart = System.currentTimeMillis();
            data.lastOpened = System.currentTimeMillis();
            return data;
        } catch (Exception e) {
            System.err.println("加载进度失败: " + e.getMessage());
            ProgressData data = new ProgressData();
            data.lastOpened = System.currentTimeMillis();
            data.lastSessionStart = data.lastOpened;
            return data;
        }
    }
    
    public static void save(ProgressData data) {
        try {
            data.lastOpened = System.currentTimeMillis();
            String json = gson.toJson(data);
            Files.write(PROGRESS_FILE, json.getBytes());
        } catch (Exception e) {
            System.err.println("保存进度失败: " + e.getMessage());
        }
    }
    
    public static void saveCurrentState(String planName, String sectionName, 
                                        int questionIndex, String questionTitle, 
                                        String code, int score) {
        ProgressData data = load();
        data.planName = planName;
        data.sectionName = sectionName;
        data.questionIndex = questionIndex;
        if (code != null && !code.isEmpty()) {
            data.savedCodes.put(questionTitle, code);
        }
        if (score >= 0) {
            data.scores.put(questionTitle, score);
        }
        save(data);
    }
    
    public static void saveCardState(String cardName, String planName, String difficulty, String sectionName, int questionIndex) {
        ProgressData data = load();
        data.currentCard = cardName;
        if (planName != null) data.planName = planName;
        if (difficulty != null) data.currentDifficulty = difficulty;
        if (sectionName != null) data.sectionName = sectionName;
        data.questionIndex = questionIndex;
        save(data);
    }
    
    public static void addSessionTime(long seconds) {
        ProgressData data = load();
        data.totalStudySeconds += seconds;
        save(data);
    }
    
    public static String getSavedCode(String questionTitle) {
        ProgressData data = load();
        if (data != null && data.savedCodes.containsKey(questionTitle)) {
            return data.savedCodes.get(questionTitle);
        }
        return null;
    }
    
    public static Integer getSavedScore(String questionTitle) {
        ProgressData data = load();
        if (data != null && data.scores.containsKey(questionTitle)) {
            return data.scores.get(questionTitle);
        }
        return null;
    }
}
