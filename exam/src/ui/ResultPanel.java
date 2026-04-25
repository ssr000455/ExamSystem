package ui;

import i18n.I18nManager;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import compiler.CodeChecker;

public class ResultPanel extends JPanel {
    private JTextPane textPane;
    private I18nManager i18n;
    
    public ResultPanel() {
        i18n = I18nManager.getInstance();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 220));
        textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        textPane.setOpaque(false);
        JScrollPane sp = new JScrollPane(textPane);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        add(sp, BorderLayout.CENTER);
        clear();
    }
    
    private String getTextColor() {
        Color fg = UIManager.getColor("Label.foreground");
        return String.format("#%02x%02x%02x", fg.getRed(), fg.getGreen(), fg.getBlue());
    }
    
    public void clear() {
        textPane.setText("<html><body style='font-family: Microsoft YaHei; color: " + getTextColor() + "; padding: 10px;'>"
            + "<p style='text-align: center; margin-top: 30px;'>" + i18n.get("result.waiting") + "</p>"
            + "</body></html>");
    }
    
    public void showInfo(String message) {
        String textColor = getTextColor();
        textPane.setText("<html><body style='font-family: Microsoft YaHei; color: " + textColor + "; padding: 10px;'>"
            + "<h3 style='color: #4CAF50;'>⏳ 处理中</h3>"
            + "<p>" + escapeHtml(message) + "</p>"
            + "</body></html>");
    }
    
    public void showCheckResult(CodeChecker.CheckResult result, int maxScore) {
        String textColor = getTextColor();
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Microsoft YaHei; color: ").append(textColor).append("; padding: 10px; margin: 0;'>");
        if (result.success) {
            html.append("<h3 style='color: #4CAF50; margin: 0 0 10px 0;'>").append(i18n.get("result.passed")).append("</h3>");
            html.append("<p style='font-size: 16px; margin: 5px 0;'>").append(i18n.get("result.score")).append(" <span style='color: #FFD700; font-weight: bold;'>").append(String.format("%.1f", result.score)).append("</span> / ").append(maxScore);
            if (result.advanced) {
                html.append(" <span style='color: #FFD700; font-weight: bold; margin-left: 8px;'>优</span>");
            }
            html.append("</p>");
        } else {
            html.append("<h3 style='color: #f44336; margin: 0 0 10px 0;'>").append(i18n.get("result.failed")).append("</h3>");
            html.append("<p style='font-size: 16px; margin: 5px 0;'>").append(i18n.get("result.score")).append(" <span style='color: #f44336; font-weight: bold;'>0</span> / ").append(maxScore).append("</p>");
        }
        if (result.checks != null && !result.checks.isEmpty()) {
            html.append("<p style='font-weight: bold; margin: 15px 0 5px 0;'>检查项目:</p>");
            html.append("<table style='border-collapse: collapse; width: 100%;'>");
            for (Map.Entry<String, CodeChecker.ItemStatus> entry : result.checks.entrySet()) {
                String color = entry.getValue().passed ? "#4CAF50" : "#f44336";
                String icon = entry.getValue().passed ? "✓" : "✗";
                html.append("<tr><td style='padding: 3px 5px;'>").append(entry.getKey()).append("</td><td style='color: ").append(color).append(";'>").append(icon).append("</td><td style='font-size: 12px;'>").append(entry.getValue().message).append("</td></tr>");
            }
            html.append("</table>");
        }
        if (result.errors != null && !result.errors.isEmpty()) {
            html.append("<p style='color: #f44336; font-weight: bold; margin: 15px 0 5px 0;'>").append(i18n.get("result.errors")).append("</p>");
            html.append("<ul style='color: #ff8a80; margin: 5px 0; padding-left: 20px;'>");
            for (String error : result.errors) html.append("<li>").append(escapeHtml(error)).append("</li>");
            html.append("</ul>");
        }
        if (result.warnings != null && !result.warnings.isEmpty()) {
            html.append("<p style='color: #FFA500; font-weight: bold; margin: 15px 0 5px 0;'>").append(i18n.get("result.warnings")).append("</p>");
            html.append("<ul style='color: #ffcc80; margin: 5px 0; padding-left: 20px;'>");
            for (String warning : result.warnings) html.append("<li>").append(escapeHtml(warning)).append("</li>");
            html.append("</ul>");
        }
        html.append("</body></html>");
        textPane.setText(html.toString());
    }
    
    public void showChoiceResult(boolean correct, int selected, int answer, int maxScore) {
        String textColor = getTextColor();
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Microsoft YaHei; color: ").append(textColor).append("; padding: 10px;'>");
        if (correct) {
            html.append("<h3 style='color: #4CAF50;'>").append(i18n.get("choice.correct")).append("</h3>");
            html.append("<p>").append(i18n.get("result.score")).append(" <span style='color: #FFD700;'>").append(maxScore).append("</span> / ").append(maxScore).append("</p>");
        } else {
            html.append("<h3 style='color: #f44336;'>").append(i18n.get("choice.wrong")).append("</h3>");
            html.append("<p>").append(i18n.get("choice.yourAnswer")).append(" <span style='color: #f44336;'>").append((char)('A' + selected)).append("</span></p>");
            html.append("<p>").append(i18n.get("choice.correctAnswer")).append(" <span style='color: #4CAF50;'>").append((char)('A' + answer)).append("</span></p>");
            html.append("<p>").append(i18n.get("result.score")).append(" 0 / ").append(maxScore).append("</p>");
        }
        html.append("</body></html>");
        textPane.setText(html.toString());
    }
    
    public void showError(String message) {
        String textColor = getTextColor();
        textPane.setText("<html><body style='font-family: Microsoft YaHei; color: " + textColor + "; padding: 10px;'>"
            + "<h3 style='color: #f44336;'>错误</h3>"
            + "<p style='color: #ff8a80;'>" + escapeHtml(message) + "</p>"
            + "</body></html>");
    }
    
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }
}
