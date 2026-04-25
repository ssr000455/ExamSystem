package progress;

import model.*;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class UnlockManager {
    private static final Path UNLOCK_FILE = Paths.get(".exam_unlocks.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private static Set<String> unlockedPlans = new HashSet<>();
    private static Map<String, Integer> planStars = new HashMap<>();
    private static int totalStars = 0;
    
    static {
        load();
    }
    
    public static void load() {
        // 默认解锁所有入门级计划
        for (int i = 1; i <= 8; i++) {
            unlockedPlans.add("plan" + i + "_BEGINNER");
        }
        
        if (Files.exists(UNLOCK_FILE)) {
            try {
                String content = Files.readString(UNLOCK_FILE);
                JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
                
                if (obj.has("unlockedPlans")) {
                    JsonArray arr = obj.getAsJsonArray("unlockedPlans");
                    for (JsonElement e : arr) {
                        unlockedPlans.add(e.getAsString());
                    }
                }
                
                if (obj.has("planStars")) {
                    JsonObject starsObj = obj.getAsJsonObject("planStars");
                    for (Map.Entry<String, JsonElement> e : starsObj.entrySet()) {
                        planStars.put(e.getKey(), e.getValue().getAsInt());
                    }
                }
                
                if (obj.has("totalStars")) {
                    totalStars = obj.get("totalStars").getAsInt();
                }
            } catch (Exception e) {
                System.err.println("加载解锁数据失败: " + e.getMessage());
            }
        }
        
        recalculateTotalStars();
    }
    
    public static void save() {
        try {
            JsonObject obj = new JsonObject();
            
            JsonArray plansArray = new JsonArray();
            for (String p : unlockedPlans) {
                plansArray.add(p);
            }
            obj.add("unlockedPlans", plansArray);
            
            JsonObject starsObj = new JsonObject();
            for (Map.Entry<String, Integer> e : planStars.entrySet()) {
                starsObj.addProperty(e.getKey(), e.getValue());
            }
            obj.add("planStars", starsObj);
            obj.addProperty("totalStars", totalStars);
            
            Files.writeString(UNLOCK_FILE, gson.toJson(obj));
        } catch (Exception e) {
            System.err.println("保存解锁数据失败: " + e.getMessage());
        }
    }
    
    public static boolean isPlanUnlocked(String planKey) {
        return unlockedPlans.contains(planKey);
    }
    
    public static boolean isPlanUnlocked(String planName, DifficultyLevel level) {
        return isPlanUnlocked(planName + "_" + level.name());
    }
    
    public static void unlockPlan(String planKey) {
        if (unlockedPlans.add(planKey)) {
            save();
        }
    }
    
    public static void addStars(String planKey, int stars) {
        planStars.put(planKey, Math.min(3, planStars.getOrDefault(planKey, 0) + stars));
        recalculateTotalStars();
        checkAutoUnlocks();
        save();
    }
    
    private static void recalculateTotalStars() {
        totalStars = planStars.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    private static void checkAutoUnlocks() {
        // 获得 15 颗星解锁普通级
        if (totalStars >= 15) {
            for (int i = 1; i <= 8; i++) {
                unlockedPlans.add("plan" + i + "_INTERMEDIATE");
            }
        }
        
        // 获得 30 颗星解锁专业级
        if (totalStars >= 30) {
            for (int i = 1; i <= 8; i++) {
                unlockedPlans.add("plan" + i + "_ADVANCED");
            }
        }
    }
    
    public static int getPlanStars(String planKey) {
        return planStars.getOrDefault(planKey, 0);
    }
    
    public static int getTotalStars() {
        return totalStars;
    }
    
    // 完成框题时调用
    public static void onSectionCompleted(String planKey, double scorePercent) {
        int stars = 0;
        if (scorePercent >= 0.9) stars = 3;
        else if (scorePercent >= 0.7) stars = 2;
        else if (scorePercent >= 0.5) stars = 1;
        
        int currentStars = getPlanStars(planKey);
        if (stars > currentStars) {
            addStars(planKey, stars - currentStars);
        }
    }
}
