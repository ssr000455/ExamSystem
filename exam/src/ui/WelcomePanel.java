package ui;

import i18n.I18nManager;
import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends JPanel {
    private I18nManager i18n;
    
    public WelcomePanel() {
        i18n = I18nManager.getInstance();
        setLayout(new BorderLayout());
        rebuild();
    }
    
    public void rebuild() {
        removeAll();
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JLabel titleLabel = new JLabel(i18n.get("app.title"));
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 32));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel versionLabel = new JLabel("版本 2.0");
        versionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        versionLabel.setForeground(UIManager.getColor("Label.foreground"));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(versionLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        JTextPane introPane = new JTextPane();
        introPane.setContentType("text/html");
        introPane.setEditable(false);
        introPane.setOpaque(false);
        introPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        Color fg = UIManager.getColor("Label.foreground");
        String textColor = String.format("#%02x%02x%02x", fg.getRed(), fg.getGreen(), fg.getBlue());
        String html = String.format(
            "<html><body style='font-family: Microsoft YaHei; color: %s; text-align: center;'>" +
            "<h2 style='color: #4b6eaf;'>%s</h2>" +
            "<p style='line-height: 1.8;'>%s</p>" +
            "<p style='line-height: 1.8;'>%s</p>" +
            "<p style='line-height: 1.8; color: #FFA500;'>💡 %s</p>" +
            "</body></html>",
            textColor, i18n.get("welcome.title"), i18n.get("welcome.desc1"), i18n.get("welcome.desc2"), i18n.get("welcome.importHint")
        );
        introPane.setText(html);
        introPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(introPane);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        infoPanel.setOpaque(false);
        infoPanel.setMaximumSize(new Dimension(800, 100));
        infoPanel.add(createStatCard("📚", i18n.get("welcome.plans"), i18n.get("welcome.plansDesc")));
        infoPanel.add(createStatCard("🎯", i18n.get("welcome.sections"), i18n.get("welcome.sectionsDesc")));
        infoPanel.add(createStatCard("💡", i18n.get("welcome.questions"), i18n.get("welcome.questionsDesc")));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(infoPanel);
        
        add(contentPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);
        JButton importBtn = new JButton("📦 " + i18n.get("menu.importPlan"));
        importBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        importBtn.addActionListener(e -> { Container p = getParent(); while(p != null && !(p instanceof MainFrame)) p = p.getParent(); if(p instanceof MainFrame) ((MainFrame)p).showImportDialog(); });
        JButton startBtn = new JButton("🚀 " + i18n.get("menu.selectPlan"));
        startBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        startBtn.addActionListener(e -> { Container p = getParent(); while(p != null && !(p instanceof MainFrame)) p = p.getParent(); if(p instanceof MainFrame) ((MainFrame)p).showPlanSelector(); });
        buttonPanel.add(importBtn);
        buttonPanel.add(startBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        
        revalidate();
        repaint();
    }
    
    private JPanel createStatCard(String icon, String title, String desc) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIManager.getColor("Panel.background"));
        Color borderColor = UIManager.getColor("Component.borderColor");
        if (borderColor == null) borderColor = UIManager.getColor("Separator.foreground");
        if (borderColor == null) borderColor = Color.GRAY;
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        JLabel iconLabel = new JLabel(icon); iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36)); iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titleLabel = new JLabel(title); titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16)); titleLabel.setForeground(UIManager.getColor("Label.foreground")); titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel descLabel = new JLabel(desc); descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12)); descLabel.setForeground(UIManager.getColor("Label.foreground")); descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel); card.add(Box.createRigidArea(new Dimension(0,10))); card.add(titleLabel); card.add(Box.createRigidArea(new Dimension(0,5))); card.add(descLabel);
        return card;
    }
}
