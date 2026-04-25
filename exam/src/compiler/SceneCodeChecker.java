package compiler;

import model.*;
import java.util.*;
import java.util.regex.*;

public class SceneCodeChecker {
    
    public static class SceneCheckResult {
        public boolean success;
        public double score;
        public int maxScore;
        public List<String> triggeredInteractions = new ArrayList<>();
        public List<String> missedInteractions = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
        public Map<String, Object> simulationData = new LinkedHashMap<>();
        public boolean advanced = false;
    }
    
    public static SceneCheckResult checkSceneCode(String code, Question question, 
                                                   SceneConfig sceneConfig) {
        SceneCheckResult result = new SceneCheckResult();
        result.maxScore = question.maxScore;
        
        // 1. 检测必须包含的关键词/模式
        checkRequiredPatterns(code, question, result);
        
        // 2. 模拟执行用户代码，检测交互触发
        simulateUserCode(code, sceneConfig, result);
        
        // 3. 检测高级特性
        if (isAdvancedSceneCode(code)) {
            result.advanced = true;
        }
        
        // 4. 计算分数
        calculateSceneScore(result, question);
        
        result.success = result.errors.isEmpty() && result.triggeredInteractions.size() > 0;
        
        return result;
    }
    
    private static void checkRequiredPatterns(String code, Question question, 
                                               SceneCheckResult result) {
        if (question.keywords != null) {
            for (String kw : question.keywords) {
                if (!code.contains(kw)) {
                    result.errors.add("缺少关键词: " + kw);
                }
            }
        }
        
        // 检测必须的 API 调用模式
        String[] requiredPatterns = {
            "moveForward", "turnLeft", "turnRight", "jump",
            "getBlockInFront", "getPosition", "interact"
        };
        
        int patternCount = 0;
        for (String pattern : requiredPatterns) {
            if (code.contains(pattern)) {
                patternCount++;
            }
        }
        
        if (patternCount < 2) {
            result.errors.add("代码中使用的 API 调用过少，请使用更多移动和交互方法");
        }
    }
    
    private static void simulateUserCode(String code, SceneConfig sceneConfig, 
                                          SceneCheckResult result) {
        // 模拟执行用户代码
        // 实际项目中可以通过 JavaCompiler 动态编译执行
        
        // 解析代码中的交互调用
        if (sceneConfig.interactions != null) {
            for (SceneConfig.InteractionPoint ip : sceneConfig.interactions) {
                String interactionPattern = "interact\\(\"" + ip.id + "\"\\)";
                String inspectPattern = "inspect\\(\"" + ip.id + "\"\\)";
                String collectPattern = "collect\\(\"" + ip.id + "\"\\)";
                
                boolean triggered = code.contains(interactionPattern) || 
                                   code.contains(inspectPattern) ||
                                   code.contains(collectPattern);
                
                if (triggered) {
                    result.triggeredInteractions.add(ip.id);
                } else {
                    result.missedInteractions.add(ip.id);
                }
            }
        }
        
        // 检测移动逻辑
        if (code.contains("moveForward()") || code.contains("moveTo(")) {
            result.simulationData.put("hasMovement", true);
        }
        
        // 检测条件判断
        if (code.contains("if") && code.contains("getBlockInFront")) {
            result.simulationData.put("hasConditionalLogic", true);
        }
        
        // 检测循环
        if (code.contains("for") || code.contains("while")) {
            result.simulationData.put("hasLoop", true);
        }
    }
    
    private static boolean isAdvancedSceneCode(String code) {
        // 检测高级模式
        boolean hasPathfinding = code.contains("findPath") || code.contains("AStar") || 
                                 code.contains("dijkstra");
        boolean hasStateMachine = code.contains("enum State") || code.contains("switch") && 
                                  code.contains("case");
        boolean hasOptimization = code.contains("PriorityQueue") || code.contains("HashMap") &&
                                  code.contains("visited");
        boolean hasPrediction = code.contains("predict") || code.contains("estimate");
        
        return hasPathfinding || hasStateMachine || hasOptimization || hasPrediction;
    }
    
    private static void calculateSceneScore(SceneCheckResult result, Question question) {
        double baseScore = 0;
        
        // 基础分：关键词和模式 (40%)
        if (result.errors.isEmpty()) {
            baseScore += question.maxScore * 0.4;
        }
        
        // 交互分：触发的交互点比例 (40%)
        if (result.triggeredInteractions.size() + result.missedInteractions.size() > 0) {
            double ratio = (double) result.triggeredInteractions.size() / 
                          (result.triggeredInteractions.size() + result.missedInteractions.size());
            baseScore += question.maxScore * 0.4 * ratio;
        }
        
        // 逻辑分：条件判断和循环 (20%)
        if (result.simulationData.getOrDefault("hasConditionalLogic", false).equals(true)) {
            baseScore += question.maxScore * 0.1;
        }
        if (result.simulationData.getOrDefault("hasLoop", false).equals(true)) {
            baseScore += question.maxScore * 0.1;
        }
        
        result.score = baseScore;
        
        // 高级代码加分
        if (result.advanced) {
            result.score = Math.min(result.score + 1.5, question.maxScore + 2);
        }
        
        result.score = Math.round(result.score * 10) / 10.0;
    }
}
