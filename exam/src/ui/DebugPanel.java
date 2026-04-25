package ui;

import i18n.I18nManager;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DebugPanel extends JPanel {
    private I18nManager i18n;
    private JTextArea buggyCodeArea;
    private JTextField errorInputField;
    private JButton checkButton;
    private JLabel statusLabel;
    private JPanel foundErrorsPanel;
    
    private String buggyCode;
    private String[] expectedErrors;
    private String fixedCode;
    private Set<String> foundErrors = new HashSet<>();
    
    public DebugPanel() {
        i18n = I18nManager.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 上半部分：错误代码展示
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("有错误的代码"));
        buggyCodeArea = new JTextArea();
        buggyCodeArea.setEditable(false);
        buggyCodeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane codeScroll = new JScrollPane(buggyCodeArea);
        topPanel.add(codeScroll, BorderLayout.CENTER);
        
        // 下半部分：错误查找和修复
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(new JLabel("发现的错误（用逗号分隔）："), BorderLayout.WEST);
        errorInputField = new JTextField();
        inputPanel.add(errorInputField, BorderLayout.CENTER);
        
        checkButton = new JButton("检查错误");
        inputPanel.add(checkButton, BorderLayout.EAST);
        
        foundErrorsPanel = new JPanel();
        foundErrorsPanel.setLayout(new BoxLayout(foundErrorsPanel, BoxLayout.Y_AXIS));
        foundErrorsPanel.setBorder(BorderFactory.createTitledBorder("已发现的错误"));
        
        JPanel fixPanel = new JPanel(new BorderLayout());
        JButton showFixButton = new JButton("显示参考答案");
        fixPanel.add(showFixButton, BorderLayout.EAST);
        
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(foundErrorsPanel), BorderLayout.CENTER);
        bottomPanel.add(fixPanel, BorderLayout.SOUTH);
        
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        add(splitPane, BorderLayout.CENTER);
        
        // 状态栏
        statusLabel = new JLabel("找出代码中的错误");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        add(statusLabel, BorderLayout.SOUTH);
        
        // 事件绑定
        checkButton.addActionListener(e -> checkErrors());
        showFixButton.addActionListener(e -> showFixedCode());
    }
    
    public void setQuestion(String buggyCode, String[] expectedErrors, String fixedCode) {
        this.buggyCode = buggyCode;
        this.expectedErrors = expectedErrors;
        this.fixedCode = fixedCode;
        
        buggyCodeArea.setText(buggyCode);
        buggyCodeArea.setCaretPosition(0);
        foundErrors.clear();
        foundErrorsPanel.removeAll();
        errorInputField.setText("");
        statusLabel.setText("找出代码中的 " + expectedErrors.length + " 个错误");
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
    }
    
    private void checkErrors() {
        String input = errorInputField.getText().trim();
        if (input.isEmpty()) return;
        
        String[] userErrors = input.split("[,，]");
        for (String err : userErrors) {
            String trimmed = err.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                foundErrors.add(trimmed);
            }
        }
        
        updateFoundErrorsDisplay();
        
        // 检查是否找全了预期错误
        int matched = 0;
        for (String expected : expectedErrors) {
            if (foundErrors.contains(expected.toLowerCase())) {
                matched++;
            }
        }
        
        if (matched == expectedErrors.length) {
            statusLabel.setText("✓ 已找出所有 " + expectedErrors.length + " 个错误！");
            statusLabel.setForeground(new Color(75, 175, 80));
        } else {
            statusLabel.setText("已找出 " + matched + "/" + expectedErrors.length + " 个错误");
            statusLabel.setForeground(UIManager.getColor("Label.foreground"));
        }
    }
    
    private void updateFoundErrorsDisplay() {
        foundErrorsPanel.removeAll();
        for (String err : foundErrors) {
            JLabel label = new JLabel("• " + err);
            label.setForeground(new Color(255, 165, 0));
            foundErrorsPanel.add(label);
        }
        foundErrorsPanel.revalidate();
        foundErrorsPanel.repaint();
    }
    
    private void showFixedCode() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "参考答案", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea fixedArea = new JTextArea(fixedCode);
        fixedArea.setEditable(false);
        fixedArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panel.add(new JScrollPane(fixedArea), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    public double getScore(int maxScore) {
        if (expectedErrors == null || expectedErrors.length == 0) return 0;
        int matched = 0;
        for (String expected : expectedErrors) {
            if (foundErrors.contains(expected.toLowerCase())) {
                matched++;
            }
        }
        return (double) matched / expectedErrors.length * maxScore;
    }
    
    public void clear() {
        buggyCodeArea.setText("");
        errorInputField.setText("");
        foundErrors.clear();
        foundErrorsPanel.removeAll();
        statusLabel.setText("找出代码中的错误");
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
    }
}
