package model;

import java.util.List;
import java.util.ArrayList;

public class Plan {
    public String name;
    public String baseName;
    public List<PlanLevel> difficultyLevels = new ArrayList<>();
    
    public PlanLevel getLevel(DifficultyLevel level) {
        for (PlanLevel pl : difficultyLevels) {
            if (pl.difficulty == level) return pl;
        }
        return null;
    }
}
