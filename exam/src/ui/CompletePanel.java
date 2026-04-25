package ui;

import i18n.I18nManager;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CompletePanel extends JPanel {
    private I18nManager i18n;
    private JTextPane codePane;
    private JLabel statusLabel;
    private JButton checkButton;
    
    private String partialCode;
    private String[] requiredElements;
    private StyledDocument doc;
    
    // 标记缺失元素的样式
    private final Style missingStyle;
    
    public CompletePanel() {
        i18n = I18nManager.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        codePane = new JTextPane();
        codePane.setFont(new Font("Monospaced", Font.PLAIN, 14));
        doc = codePane.getStyledDocument();
        
        missingStyle = doc.addStyle("Missing", null);
        StyleConstants.setBackground(missingStyle, new Color(255, 200, 200));
        StyleConstants.setBold(missingStyle, true);
        
        JScrollPane scrollPane = new JScrollPane(codePane);
        scrollPane.setBorder(BorderFactory.createTitledBorder("补全代码"));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        checkButton = new JButton("检查补全");
        bottomPanel.add(checkButton, BorderLayout.EAST);
        
        statusLabel = new JLabel("请补全标记的区域");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        checkButton.addActionListener(e -> checkCompletion());
    }
    
    public void setQuestion(String partialCode, String[] requiredElements) {
        this.partialCode = partialCode;
        this.requiredElements = requiredElements;
        
        // 将 ____ 替换为可编辑的占位符样式
        String displayCode = partialCode.replace("____", "__________");
        codePane.setText(displayCode);
        
        // 高亮显示所有 ____ 区域（实际上已被替换）
        String text = codePane.getText();
        int index = 0;
        while ((index = text.indexOf("__________", index)) >= 0) {
            doc.setCharacterAttributes(index, 10, missingStyle, false);
            index += 10;
        }
        
        statusLabel.setText("需要补全 " + requiredElements.length + " 个元素");
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
    }
    
    private void checkCompletion() {
        String userCode = codePane.getText();
        
        // 检查是否所有占位符都已被替换
        if (userCode.contains("__________")) {
            statusLabel.setText("还有未补全的占位符");
            statusLabel.setForeground(new Color(244, 67, 54));
            return;
        }
        
        // 检查是否包含所有必需元素
        List<String> missing = new ArrayList<>();
        for (String elem : requiredElements) {
            if (!userCode.contains(elem)) {
                missing.add(elem);
            }
        }
        
        if (missing.isEmpty()) {
            statusLabel.setText("✓ 补全正确！");
            statusLabel.setForeground(new Color(75, 175, 80));
        } else {
            statusLabel.setText("缺少元素: " + String.join(", ", missing));
            statusLabel.setForeground(new Color(244, 67, 54));
        }
    }
    
    public double getScore(int maxScore) {
        String userCode = codePane.getText();
        if (userCode.contains("__________")) return 0;
        
        int found = 0;
        for (String elem : requiredElements) {
            if (userCode.contains(elem)) found++;
        }
        return requiredElements.length > 0 ? (double) found / requiredElements.length * maxScore : 0;
    }
    
    public String getUserCode() {
        return codePane.getText();
    }
    
    public void clear() {
        codePane.setText("");
        statusLabel.setText("请补全标记的区域");
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
    }
}
