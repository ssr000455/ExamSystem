package model;

public class Question {
    // 基础字段
    public String type;
    public String title;
    public String description;
    public int maxScore;
    public int difficulty = 2;
    public String[] tags;
    
    // 场景渲染字段
    public String scenePackage;
    public String[] modelRefs;
    public String sceneConfig;
    
    // 编程题字段
    public String template;
    public String[] keywords;
    public String[] hints;
    public String reference;
    
    // 选择题字段
    public String[] options;
    public int answer;
    
    // 填空题字段
    public String textWithBlanks;
    public String[] blankAnswers;
    public boolean caseSensitive;
    
    // 连线题字段
    public String[] leftItems;
    public String[] rightItems;
    public int[] correctMatches;
    
    // 排序题字段
    public String[] steps;
    public int[] correctOrder;
    
    // 调试题字段
    public String buggyCode;
    public String[] expectedErrors;
    public String fixedCode;
    
    // 补全题字段
    public String partialCode;
    public String[] requiredElements;
    
    public String getDifficultyStars() {
        return "★".repeat(difficulty) + "☆".repeat(4 - difficulty);
    }
    
    public double getScoreMultiplier() {
        switch (difficulty) {
            case 1: return 0.8;
            case 3: return 1.2;
            case 4: return 1.5;
            default: return 1.0;
        }
    }
}
