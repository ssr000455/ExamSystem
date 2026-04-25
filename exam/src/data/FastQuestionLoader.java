package data;

import model.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.io.File;

public class FastQuestionLoader {
    private static final Gson gson = new Gson();
    private static List<Plan> cachedStructure;
    
    public static List<Plan> loadStructureOnly() throws Exception {
        if (cachedStructure != null) return cachedStructure;
        
        List<Plan> plans = new ArrayList<>();
        Path questionsDir = Paths.get("questions");
        if (!Files.exists(questionsDir)) return plans;
        
        File[] planDirs = questionsDir.toFile().listFiles(File::isDirectory);
        if (planDirs == null) return plans;
        
        Arrays.sort(planDirs, Comparator.comparing(File::getName));
        
        for (File planDir : planDirs) {
            Plan plan = new Plan();
            plan.baseName = planDir.getName();
            plan.name = formatPlanName(planDir.getName());
            plan.difficultyLevels = new ArrayList<>();
            
            for (String level : new String[]{"beginner", "intermediate", "advanced"}) {
                Path levelPath = planDir.toPath().resolve(level);
                if (Files.exists(levelPath)) {
                    PlanLevel planLevel = new PlanLevel();
                    planLevel.difficulty = DifficultyLevel.fromLevel(
                        level.equals("beginner") ? 0 : (level.equals("intermediate") ? 1 : 2)
                    );
                    planLevel.unlocked = level.equals("beginner");
                    File[] sectionFiles = levelPath.toFile().listFiles((d, n) -> n.endsWith(".json"));
                    if (sectionFiles != null) {
                        for (File sf : sectionFiles) {
                            Section section = new Section();
                            section.name = sf.getName().replace(".json", "");
                            planLevel.sections.add(section);
                        }
                    }
                    plan.difficultyLevels.add(planLevel);
                }
            }
            plans.add(plan);
        }
        
        cachedStructure = plans;
        return plans;
    }
    
    public static Section loadSectionFull(String planName, String levelName, String sectionName) throws Exception {
        Path sectionPath = Paths.get("questions", planName, levelName, sectionName + ".json");
        if (!Files.exists(sectionPath)) return null;
        
        String content = Files.readString(sectionPath);
        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
        Section section = new Section();
        section.name = obj.get("name").getAsString();
        
        JsonArray questionsArray = obj.getAsJsonArray("questions");
        for (JsonElement qe : questionsArray) {
            section.questions.add(QuestionLoader.parseQuestion(qe.getAsJsonObject(), 
                DifficultyLevel.fromLevel(levelName.equals("beginner") ? 0 : (levelName.equals("intermediate") ? 1 : 2))));
        }
        return section;
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
}
