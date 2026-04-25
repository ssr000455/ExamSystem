package ui;

import javax.swing.*;
import java.awt.*;

public class LoadingScreen extends JWindow {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public LoadingScreen(Frame owner) {
        super(owner);
        setSize(400, 150);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(UIManager.getColor("Panel.background"));
        
        JLabel titleLabel = new JLabel("ExamSystem 启动中...");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.CENTER);
        
        statusLabel = new JLabel("正在加载题库...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        add(panel);
    }
    
    public void setStatus(String text) {
        statusLabel.setText(text);
    }
    
    public void setProgress(int value) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(value);
    }
}
