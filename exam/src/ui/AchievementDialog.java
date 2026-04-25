package ui;

import model.Achievement;
import progress.AchievementManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AchievementDialog extends JDialog {
    
    public AchievementDialog(Frame owner) {
        super(owner, "成就", true);
        setSize(550, 450);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("成就进度");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        List<Achievement> achievements = AchievementManager.getAllAchievements();
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        
        for (Achievement a : achievements) {
            JPanel item = createAchievementItem(a);
            listPanel.add(item);
            listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        int unlocked = AchievementManager.getUnlockedCount();
        int total = AchievementManager.getTotalCount();
        JLabel statsLabel = new JLabel("已解锁: " + unlocked + "/" + total);
        statsLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        bottomPanel.add(statsLabel, BorderLayout.WEST);
        
        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton, BorderLayout.EAST);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private JPanel createAchievementItem(Achievement a) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(a.unlocked ? new Color(76, 175, 80) : Color.GRAY),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setBackground(a.unlocked ? new Color(240, 255, 240) : UIManager.getColor("Panel.background"));
        
        JLabel nameLabel = new JLabel(a.type.displayName);
        nameLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        if (a.unlocked) nameLabel.setForeground(new Color(76, 175, 80));
        
        JLabel descLabel = new JLabel(a.type.description);
        descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(nameLabel);
        textPanel.add(descLabel);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        if (a.unlocked) {
            JLabel checkLabel = new JLabel("已完成");
            checkLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
            checkLabel.setForeground(new Color(76, 175, 80));
            panel.add(checkLabel, BorderLayout.EAST);
        } else {
            JProgressBar bar = new JProgressBar(0, a.target);
            bar.setValue(a.progress);
            bar.setStringPainted(true);
            bar.setString(a.progress + "/" + a.target);
            bar.setPreferredSize(new Dimension(100, 20));
            panel.add(bar, BorderLayout.EAST);
        }
        
        return panel;
    }
}
