package ui.controller;

import model.*;
import ui.CodeEditorPanel;
import java.util.List;

public class QuestionNavigator {
    private List<Question> questions;
    private int currentIndex;
    private CodeEditorPanel editorPanel;
    
    public QuestionNavigator(CodeEditorPanel editorPanel) {
        this.editorPanel = editorPanel;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        this.currentIndex = 0;
    }
    
    public Question getCurrentQuestion() {
        return questions == null || currentIndex >= questions.size() ? null : questions.get(currentIndex);
    }
    
    public boolean hasPrev() { return currentIndex > 0; }
    public boolean hasNext() { return questions != null && currentIndex < questions.size() - 1; }
    
    public void prev() { if (hasPrev()) currentIndex--; }
    public void next() { if (hasNext()) currentIndex++; }
    
    public int getCurrentIndex() { return currentIndex; }
    public int getTotalCount() { return questions == null ? 0 : questions.size(); }
    
    public void saveCurrentCode() {
        Question q = getCurrentQuestion();
        if (q != null && "program".equals(q.type)) {
            // 保存逻辑
        }
    }
}
