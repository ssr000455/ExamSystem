package ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FillBlankPanel extends JPanel {
    private List<JTextField> blankFields = new ArrayList<>();
    private String[] correctAnswers;
    private boolean caseSensitive;
    private JLabel statusLabel;
    
    public FillBlankPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
    
    public void setQuestion(String textWithBlanks, String[] answers, boolean caseSensitive) {
        removeAll();
        blankFields.clear();
        this.correctAnswers = answers;
        this.caseSensitive = caseSensitive;
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));
        
        // 解析文本，将 ____ 替换为输入框
        String[] parts = textWithBlanks.split("_{4,}");
        
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        textPanel.setBackground(UIManager.getColor("Panel.background"));
        
        for (int i = 0; i < parts.length; i++) {
            JLabel label = new JLabel(parts[i]);
            label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            label.setForeground(UIManager.getColor("Label.foreground"));
            textPanel.add(label);
            
            if (i < parts.length - 1) {
                JTextField field = new JTextField(10);
                field.setFont(new Font("Monospaced", Font.PLAIN, 14));
                blankFields.add(field);
                textPanel.add(field);
                
                JLabel hintLabel = new JLabel("(" + (i + 1) + ")");
                hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
                hintLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
                textPanel.add(hintLabel);
            }
        }
        
        contentPanel.add(textPanel);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        statusLabel = new JLabel("请填写所有空白");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        revalidate();
        repaint();
    }
    
    public boolean checkAnswers() {
        if (blankFields.size() != correctAnswers.length) return false;
        
        boolean allCorrect = true;
        List<Integer> wrongIndices = new ArrayList<>();
        
        for (int i = 0; i < blankFields.size(); i++) {
            String userAnswer = blankFields.get(i).getText().trim();
            String correct = correctAnswers[i];
            
            boolean match = caseSensitive ? 
                userAnswer.equals(correct) : 
                userAnswer.equalsIgnoreCase(correct);
            
            if (!match) {
                allCorrect = false;
                wrongIndices.add(i + 1);
            }
            
            // 视觉反馈
            blankFields.get(i).setBackground(match ? 
                new Color(200, 255, 200) : 
                new Color(255, 200, 200));
        }
        
        if (allCorrect) {
            statusLabel.setText("全部正确");
            statusLabel.setForeground(new Color(75, 175, 80));
        } else {
            statusLabel.setText("第 " + wrongIndices + " 空错误");
            statusLabel.setForeground(new Color(244, 67, 54));
        }
        
        return allCorrect;
    }
    
    public double getScore(int maxScore) {
        if (blankFields.isEmpty()) return 0;
        int correctCount = 0;
        for (int i = 0; i < blankFields.size(); i++) {
            String userAnswer = blankFields.get(i).getText().trim();
            String correct = correctAnswers[i];
            boolean match = caseSensitive ? 
                userAnswer.equals(correct) : 
                userAnswer.equalsIgnoreCase(correct);
            if (match) correctCount++;
        }
        return (double) correctCount / blankFields.size() * maxScore;
    }
    
    public void clear() {
        for (JTextField field : blankFields) {
            field.setText("");
            field.setBackground(UIManager.getColor("TextField.background"));
        }
        statusLabel.setText("请填写所有空白");
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
    }
}
