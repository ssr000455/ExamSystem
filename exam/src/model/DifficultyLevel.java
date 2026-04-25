package model;

public enum DifficultyLevel {
    BEGINNER("入门级", 0, "基础语法与概念", 0.8),
    INTERMEDIATE("普通级", 1, "综合应用与实践", 1.0),
    ADVANCED("专业级", 2, "高级特性与优化", 1.3);
    
    public final String displayName;
    public final int level;
    public final String description;
    public final double scoreMultiplier;
    
    DifficultyLevel(String displayName, int level, String description, double scoreMultiplier) {
        this.displayName = displayName;
        this.level = level;
        this.description = description;
        this.scoreMultiplier = scoreMultiplier;
    }
    
    public static DifficultyLevel fromLevel(int level) {
        for (DifficultyLevel dl : values()) {
            if (dl.level == level) return dl;
        }
        return BEGINNER;
    }
}
