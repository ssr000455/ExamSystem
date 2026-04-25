package progress;

import model.Achievement;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementManager {
    private static final Path ACHIEVEMENT_FILE = Paths.get(".exam_achievements.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Map<Achievement.Type, Achievement> achievements = new ConcurrentHashMap<>();
    private static final Set<String> studyDays = ConcurrentHashMap.newKeySet();
    
    static {
        load();
    }
    
    public static void load() {
        for (Achievement.Type type : Achievement.Type.values()) {
            achievements.put(type, new Achievement(type));
        }
        
        if (Files.exists(ACHIEVEMENT_FILE)) {
            try {
                String content = Files.readString(ACHIEVEMENT_FILE);
                JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
                
                for (Achievement.Type type : Achievement.Type.values()) {
                    if (obj.has(type.name())) {
                        JsonObject aObj = obj.getAsJsonObject(type.name());
                        Achievement a = achievements.get(type);
                        a.unlocked = aObj.get("unlocked").getAsBoolean();
                        a.unlockTime = aObj.get("unlockTime").getAsLong();
                        a.progress = aObj.get("progress").getAsInt();
                    }
                }
                
                if (obj.has("studyDays")) {
                    JsonArray arr = obj.getAsJsonArray("studyDays");
                    for (JsonElement e : arr) {
                        studyDays.add(e.getAsString());
                    }
                }
            } catch (Exception e) {
                System.err.println("加载成就失败: " + e.getMessage());
            }
        }
    }
    
    public static void save() {
        try {
            JsonObject obj = new JsonObject();
            
            for (Map.Entry<Achievement.Type, Achievement> entry : achievements.entrySet()) {
                JsonObject aObj = new JsonObject();
                aObj.addProperty("unlocked", entry.getValue().unlocked);
                aObj.addProperty("unlockTime", entry.getValue().unlockTime);
                aObj.addProperty("progress", entry.getValue().progress);
                obj.add(entry.getKey().name(), aObj);
            }
            
            JsonArray daysArray = new JsonArray();
            for (String day : studyDays) {
                daysArray.add(day);
            }
            obj.add("studyDays", daysArray);
            
            Files.writeString(ACHIEVEMENT_FILE, gson.toJson(obj));
        } catch (Exception e) {
            System.err.println("保存成就失败: " + e.getMessage());
        }
    }
    
    public static void recordStudyDay() {
        String today = LocalDate.now().toString();
        if (studyDays.add(today)) {
            Achievement a = achievements.get(Achievement.Type.CONSISTENT);
            a.progress = studyDays.size();
            if (a.progress >= a.target) {
                a.unlock();
            }
            save();
        }
    }
    
    public static void onSectionCompleted(String planName, String sectionName, double scorePercent) {
        Achievement a = achievements.get(Achievement.Type.FIRST_SECTION);
        if (!a.unlocked) {
            a.unlock();
        }
        
        a = achievements.get(Achievement.Type.PLAN_MASTER);
        if (scorePercent >= 0.8) {
            a.unlock();
        }
        
        a = achievements.get(Achievement.Type.PERFECTIONIST);
        if (scorePercent >= 1.0) {
            a.unlock();
        }
        
        save();
    }
    
    public static void onExcellentCode() {
        Achievement a = achievements.get(Achievement.Type.EXCELLENT_CODER);
        a.progress++;
        if (a.progress >= a.target) {
            a.unlock();
        }
        save();
    }
    
    public static void onAllPlansCompleted() {
        Achievement a = achievements.get(Achievement.Type.GRAND_MASTER);
        a.unlock();
        save();
    }
    
    public static void onPlanCompletedInTime(int minutes) {
        Achievement a = achievements.get(Achievement.Type.SPEED_RUNNER);
        if (minutes <= a.target) {
            a.unlock();
            save();
        }
    }
    
    public static List<Achievement> getAllAchievements() {
        List<Achievement> list = new ArrayList<>(achievements.values());
        list.sort((a, b) -> {
            if (a.unlocked != b.unlocked) return Boolean.compare(b.unlocked, a.unlocked);
            return a.type.name().compareTo(b.type.name());
        });
        return list;
    }
    
    public static int getUnlockedCount() {
        int count = 0;
        for (Achievement a : achievements.values()) {
            if (a.unlocked) count++;
        }
        return count;
    }
    
    public static int getTotalCount() {
        return Achievement.Type.values().length;
    }
}
