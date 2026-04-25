package model;

import java.util.List;
import java.util.ArrayList;

public class PlanLevel {
    public DifficultyLevel difficulty;
    public List<Section> sections = new ArrayList<>();
    public boolean unlocked = false;
    
    public int getTotalQuestions() {
        int count = 0;
        for (Section s : sections) {
            count += s.questions.size();
        }
        return count;
    }
}
