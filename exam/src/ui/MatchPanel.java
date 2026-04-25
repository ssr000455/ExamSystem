package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MatchPanel extends JPanel {
    private String[] leftItems;
    private String[] rightItems;
    private int[] correctMatches;
    private JComboBox<String>[] matchSelectors;
    private JLabel statusLabel;
    
    @SuppressWarnings("unchecked")
    public MatchPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
    
    @SuppressWarnings("unchecked")
    public void setItems(String[] left, String[] right, int[] correct) {
        removeAll();
        this.leftItems = left;
        this.rightItems = right;
        this.correctMatches = correct;
        this.matchSelectors = new JComboBox[left.length];
        
        JPanel matchPanel = new JPanel(new GridBagLayout());
        matchPanel.setBackground(UIManager.getColor("Panel.background"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 表头
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel leftHeader = new JLabel("概念");
        leftHeader.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        matchPanel.add(leftHeader, gbc);
        
        gbc.gridx = 1;
        JLabel arrowHeader = new JLabel("");
        matchPanel.add(arrowHeader, gbc);
        
        gbc.gridx = 2;
        JLabel rightHeader = new JLabel("描述");
        rightHeader.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        matchPanel.add(rightHeader, gbc);
        
        // 左侧固定项
        for (int i = 0; i < left.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
            JLabel leftLabel = new JLabel((i + 1) + ". " + left[i]);
            leftLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            leftLabel.setForeground(UIManager.getColor("Label.foreground"));
            matchPanel.add(leftLabel, gbc);
            
            gbc.gridx = 1;
            JLabel arrow = new JLabel("→");
            arrow.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            matchPanel.add(arrow, gbc);
            
            gbc.gridx = 2;
            String[] options = new String[right.length];
            for (int j = 0; j < right.length; j++) {
                options[j] = (char)('A' + j) + ". " + right[j];
            }
            matchSelectors[i] = new JComboBox<>(options);
            matchSelectors[i].setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            matchSelectors[i].setPreferredSize(new Dimension(250, 30));
            matchPanel.add(matchSelectors[i], gbc);
        }
        
        add(new JScrollPane(matchPanel), BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        statusLabel = new JLabel("请完成所有连线");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        revalidate();
        repaint();
    }
    
    public boolean checkMatches() {
        boolean allCorrect = true;
        int correctCount = 0;
        
        for (int i = 0; i < matchSelectors.length; i++) {
            int selected = matchSelectors[i].getSelectedIndex();
            boolean isCorrect = (selected == correctMatches[i]);
            
            if (isCorrect) {
                correctCount++;
                matchSelectors[i].setBackground(new Color(200, 255, 200));
            } else {
                allCorrect = false;
                matchSelectors[i].setBackground(new Color(255, 200, 200));
            }
        }
        
        if (allCorrect) {
            statusLabel.setText("全部正确");
            statusLabel.setForeground(new Color(75, 175, 80));
        } else {
            statusLabel.setText("正确: " + correctCount + "/" + matchSelectors.length);
            statusLabel.setForeground(new Color(244, 67, 54));
        }
        
        return allCorrect;
    }
    
    public double getScore(int maxScore) {
        if (matchSelectors.length == 0) return 0;
        int correctCount = 0;
        for (int i = 0; i < matchSelectors.length; i++) {
            if (matchSelectors[i].getSelectedIndex() == correctMatches[i]) {
                correctCount++;
            }
        }
        return (double) correctCount / matchSelectors.length * maxScore;
    }
    
    public void clear() {
        for (JComboBox<String> selector : matchSelectors) {
            selector.setSelectedIndex(0);
            selector.setBackground(UIManager.getColor("ComboBox.background"));
        }
        statusLabel.setText("请完成所有连线");
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
    }
}
