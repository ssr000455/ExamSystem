package data;

import model.*;
import java.nio.file.*;
import com.google.gson.*;
import java.util.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.*;

public class QuestionLoader {
    private static final Logger LOGGER = Logger.getLogger(QuestionLoader.class.getName());
    private static final Gson gson = new Gson();
    private static volatile List<Plan> cachedPlans;
    private static final Object lock = new Object();
    private static CompletableFuture<List<Plan>> preloadFuture;
    
    public static CompletableFuture<List<Plan>> preloadAsync() {
        if (preloadFuture == null) {
            preloadFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return loadInternal();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "预加载失败", e);
                    return new ArrayList<>();
                }
            });
        }
        return preloadFuture;
    }
    
    public static List<Plan> load() throws Exception {
        if (cachedPlans != null) return cachedPlans;
        synchronized (lock) {
            if (cachedPlans == null) {
                cachedPlans = loadInternal();
            }
        }
        return cachedPlans;
    }
    
    private static List<Plan> loadInternal() throws Exception {
        List<Plan> plans = new ArrayList<>();
        Path questionsDir = Paths.get("questions");
        
        if (!Files.exists(questionsDir)) {
            Files.createDirectories(questionsDir);
            return plans;
        }
        
        File[] planDirs = questionsDir.toFile().listFiles(File::isDirectory);
        if (planDirs == null) return plans;
        
        Arrays.sort(planDirs, Comparator.comparing(File::getName));
        
        for (File planDir : planDirs) {
            Plan plan = new Plan();
            plan.baseName = planDir.getName();
            plan.name = formatPlanName(planDir.getName());
            plan.difficultyLevels = new ArrayList<>();
            
            // 加载入门级
            Path beginnerPath = planDir.toPath().resolve("beginner");
            if (Files.exists(beginnerPath)) {
                PlanLevel beginner = loadPlanLevel(beginnerPath.toFile(), DifficultyLevel.BEGINNER);
                if (beginner != null) {
                    beginner.unlocked = true; // 入门级默认解锁
                    plan.difficultyLevels.add(beginner);
                }
            }
            
            // 加载普通级
            Path intermediatePath = planDir.toPath().resolve("intermediate");
            if (Files.exists(intermediatePath)) {
                PlanLevel intermediate = loadPlanLevel(intermediatePath.toFile(), DifficultyLevel.INTERMEDIATE);
                if (intermediate != null) plan.difficultyLevels.add(intermediate);
            }
            
            // 加载专业级
            Path advancedPath = planDir.toPath().resolve("advanced");
            if (Files.exists(advancedPath)) {
                PlanLevel advanced = loadPlanLevel(advancedPath.toFile(), DifficultyLevel.ADVANCED);
                if (advanced != null) plan.difficultyLevels.add(advanced);
            }
            
            if (!plan.difficultyLevels.isEmpty()) {
                plans.add(plan);
            }
        }
        
        return plans;
    }
    
    private static PlanLevel loadPlanLevel(File levelDir, DifficultyLevel level) {
        PlanLevel planLevel = new PlanLevel();
        planLevel.difficulty = level;
        
        File[] sectionFiles = levelDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (sectionFiles == null) return null;
        
        Arrays.sort(sectionFiles, Comparator.comparing(File::getName));
        
        for (File sectionFile : sectionFiles) {
            try {
                String content = Files.readString(sectionFile.toPath(), StandardCharsets.UTF_8);
                JsonObject sectionObj = JsonParser.parseString(content).getAsJsonObject();
                
                Section section = new Section();
                section.name = sectionObj.get("name").getAsString();
                
                JsonArray questionsArray = sectionObj.getAsJsonArray("questions");
                for (JsonElement qe : questionsArray) {
                    Question q = parseQuestion(qe.getAsJsonObject(), level);
                    section.questions.add(q);
                }
                planLevel.sections.add(section);
            } catch (Exception e) {
                LOGGER.warning("加载失败: " + sectionFile.getPath() + " - " + e.getMessage());
            }
        }
        
        return planLevel.sections.isEmpty() ? null : planLevel;
    }
    
    public static Question parseQuestion(JsonObject qo, DifficultyLevel level) {
        Question q = new Question();
        q.type = qo.get("type").getAsString();
        q.title = qo.get("title").getAsString();
        q.maxScore = qo.get("maxScore").getAsInt();
        q.difficulty = level.level + 1;
        
        if (qo.has("description")) q.description = qo.get("description").getAsString();
        if (qo.has("scenePackage")) q.scenePackage = qo.get("scenePackage").getAsString();
        
        if ("program".equals(q.type)) {
            q.template = qo.get("template").getAsString();
            JsonArray ka = qo.getAsJsonArray("keywords");
            q.keywords = new String[ka.size()];
            for (int i = 0; i < ka.size(); i++) q.keywords[i] = ka.get(i).getAsString();
            if (qo.has("hints")) {
                JsonArray ha = qo.getAsJsonArray("hints");
                q.hints = new String[ha.size()];
                for (int i = 0; i < ha.size(); i++) q.hints[i] = ha.get(i).getAsString();
            }
            if (qo.has("reference")) q.reference = qo.get("reference").getAsString();
        }
        else if ("choice".equals(q.type)) {
            JsonArray oa = qo.getAsJsonArray("options");
            q.options = new String[oa.size()];
            for (int i = 0; i < oa.size(); i++) q.options[i] = oa.get(i).getAsString();
            q.answer = qo.get("answer").getAsInt();
        }
        else if ("fill".equals(q.type)) {
            q.textWithBlanks = qo.get("text").getAsString();
            JsonArray ba = qo.getAsJsonArray("answers");
            q.blankAnswers = new String[ba.size()];
            for (int i = 0; i < ba.size(); i++) q.blankAnswers[i] = ba.get(i).getAsString();
            q.caseSensitive = qo.has("caseSensitive") && qo.get("caseSensitive").getAsBoolean();
        }
        else if ("match".equals(q.type)) {
            JsonArray la = qo.getAsJsonArray("leftItems");
            q.leftItems = new String[la.size()];
            for (int i = 0; i < la.size(); i++) q.leftItems[i] = la.get(i).getAsString();
            JsonArray ra = qo.getAsJsonArray("rightItems");
            q.rightItems = new String[ra.size()];
            for (int i = 0; i < ra.size(); i++) q.rightItems[i] = ra.get(i).getAsString();
            JsonArray ma = qo.getAsJsonArray("matches");
            q.correctMatches = new int[ma.size()];
            for (int i = 0; i < ma.size(); i++) q.correctMatches[i] = ma.get(i).getAsInt();
        }
        else if ("order".equals(q.type)) {
            JsonArray sa = qo.getAsJsonArray("steps");
            q.steps = new String[sa.size()];
            for (int i = 0; i < sa.size(); i++) q.steps[i] = sa.get(i).getAsString();
            JsonArray oa = qo.getAsJsonArray("correctOrder");
            q.correctOrder = new int[oa.size()];
            for (int i = 0; i < oa.size(); i++) q.correctOrder[i] = oa.get(i).getAsInt();
        }
        else if ("debug".equals(q.type)) {
            q.buggyCode = qo.get("buggyCode").getAsString();
            JsonArray ea = qo.getAsJsonArray("expectedErrors");
            q.expectedErrors = new String[ea.size()];
            for (int i = 0; i < ea.size(); i++) q.expectedErrors[i] = ea.get(i).getAsString();
            q.fixedCode = qo.get("fixedCode").getAsString();
        }
        else if ("complete".equals(q.type)) {
            q.partialCode = qo.get("partialCode").getAsString();
            JsonArray re = qo.getAsJsonArray("requiredElements");
            q.requiredElements = new String[re.size()];
            for (int i = 0; i < re.size(); i++) q.requiredElements[i] = re.get(i).getAsString();
        }
        
        return q;
    }
    
    private static String formatPlanName(String dirName) {
        if (dirName.startsWith("plan")) {
            try {
                int num = Integer.parseInt(dirName.substring(4));
                return "计划" + num + ": " + getPlanDescription(num);
            } catch (NumberFormatException e) {
                return dirName;
            }
        }
        return dirName;
    }
    
    private static String getPlanDescription(int planNum) {
        switch (planNum) {
            case 1: return "基础物品与方块";
            case 2: return "数据与资源管理";
            case 3: return "世界生成与维度";
            case 4: return "游戏机制扩展";
            case 5: return "高级系统定制";
            case 6: return "网络与GUI";
            case 7: return "渲染与API设计";
            case 8: return "优化与发布";
            default: return "自定义计划";
        }
    }
    
    public static void clearCache() {
        synchronized (lock) {
            cachedPlans = null;
            preloadFuture = null;
        }
    }
}
