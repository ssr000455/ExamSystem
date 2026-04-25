package model;

import java.util.*;
import java.time.Instant;

public class Achievement {
    public enum Type {
        FIRST_SECTION("初窥门径", "完成第一个框题"),
        PLAN_MASTER("融会贯通", "单个计划得分超过80%"),
        EXCELLENT_CODER("精益求精", "获得5次\"优\"评价"),
        GRAND_MASTER("登峰造极", "完成全部8个计划"),
        SPEED_RUNNER("速通达人", "30分钟内完成一个计划"),
        PERFECTIONIST("完美主义", "单个框题获得满分"),
        CONSISTENT("持之以恒", "连续学习7天");
        
        public final String displayName;
        public final String description;
        
        Type(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }
    
    public Type type;
    public boolean unlocked;
    public long unlockTime;
    public int progress;
    public int target;
    
    public Achievement(Type type) {
        this.type = type;
        this.unlocked = false;
        this.progress = 0;
        this.target = getDefaultTarget(type);
    }
    
    private int getDefaultTarget(Type type) {
        switch (type) {
            case EXCELLENT_CODER: return 5;
            case SPEED_RUNNER: return 30;
            case CONSISTENT: return 7;
            default: return 1;
        }
    }
    
    public double getProgressPercent() {
        return Math.min(1.0, (double) progress / target);
    }
    
    public void unlock() {
        if (!unlocked) {
            unlocked = true;
            unlockTime = Instant.now().getEpochSecond();
        }
    }
}
