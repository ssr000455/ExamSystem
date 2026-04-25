import java.util.*;
package data;

import model.*;
import com.google.gson.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class PlanImporter {
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(".json", ".png", ".jpg", ".txt", ".md", ".properties"));
    
    private static boolean isAllowedFile(String name) {
        for (String ext : ALLOWED_EXT) {
            if (name.toLowerCase().endsWith(ext)) return true;
        }
        return false;
    }

    
    public static class ImportResult {
        public boolean success;
        public String planName;
        public int sectionCount;
        public int questionCount;
        public java.util.List<String> errors = new ArrayList<>();
        public java.util.List<String> warnings = new ArrayList<>();
    }
    
    public static ImportResult importPlan(File zipFile, JFrame parent) {
        ImportResult result = new ImportResult();
        
        JDialog progressDialog = new JDialog(parent, "导入计划", true);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel statusLabel = new JLabel("正在准备导入...");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        
        JTextArea logArea = new JTextArea(5, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        
        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(logScroll, BorderLayout.SOUTH);
        
        progressDialog.add(panel);
        
        new Thread(() -> {
            try {
                updateStatus(statusLabel, progressBar, logArea, "正在解压文件...", 10);
                
                if (!zipFile.exists() || !zipFile.getName().endsWith(".zip")) {
                    result.errors.add("无效的ZIP文件");
                    result.success = false;
                    SwingUtilities.invokeLater(() -> progressDialog.dispose());
                    return;
                }
                
                updateStatus(statusLabel, progressBar, logArea, "正在读取计划信息...", 20);
                
                Path tempDir = Files.createTempDirectory("plan_import_");
                tempDir.toFile().deleteOnExit();
                
                unzip(zipFile, tempDir.toFile(), logArea);
                
                updateStatus(statusLabel, progressBar, logArea, "正在解析计划结构...", 40);
                
                File[] planDirs = tempDir.toFile().listFiles(File::isDirectory);
                if (planDirs == null || planDirs.length == 0) {
                    result.errors.add("ZIP文件中没有找到计划目录");
                    result.success = false;
                    SwingUtilities.invokeLater(() -> progressDialog.dispose());
                    return;
                }
                
                File planDir = planDirs[0];
                String planName = planDir.getName();
                result.planName = planName;
                
                updateStatus(statusLabel, progressBar, logArea, "正在验证格式...", 50);
                
                File[] jsonFiles = planDir.listFiles((d, name) -> name.endsWith(".json"));
                if (jsonFiles == null || jsonFiles.length == 0) {
                    result.errors.add("没有找到框题JSON文件");
                    result.success = false;
                    SwingUtilities.invokeLater(() -> progressDialog.dispose());
                    return;
                }
                
                Arrays.sort(jsonFiles);
                result.sectionCount = jsonFiles.length;
                
                updateStatus(statusLabel, progressBar, logArea, "正在导入题目...", 60);
                
                Path targetDir = Paths.get("questions", planName);
                Files.createDirectories(targetDir);
                
                int totalQuestions = 0;
                
                for (int i = 0; i < jsonFiles.length; i++) {
                    File jsonFile = jsonFiles[i];
                    int progress = 60 + (i * 30 / jsonFiles.length);
                    updateStatus(statusLabel, progressBar, logArea, 
                        "正在导入: " + jsonFile.getName(), progress);
                    
                    try {
                        String content = new String(Files.readAllBytes(jsonFile.toPath()), "UTF-8");
                        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
                        
                        if (!obj.has("name") || !obj.has("questions")) {
                            result.warnings.add(jsonFile.getName() + " 格式不正确，跳过");
                            continue;
                        }
                        
                        JsonArray questions = obj.getAsJsonArray("questions");
                        totalQuestions += questions.size();
                        
                        Path targetFile = targetDir.resolve(jsonFile.getName());
                        Files.copy(jsonFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
                        
                    } catch (Exception e) {
                        result.warnings.add("导入 " + jsonFile.getName() + " 失败: " + e.getMessage());
                    }
                }
                
                result.questionCount = totalQuestions;
                result.success = true;
                
                updateStatus(statusLabel, progressBar, logArea, "导入完成", 100);
                
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    showImportResult(result, parent);
                });
                
            } catch (Exception e) {
                result.errors.add("导入失败: " + e.getMessage());
                result.success = false;
                SwingUtilities.invokeLater(() -> progressDialog.dispose());
            }
        }).start();
        
        progressDialog.setVisible(true);
        return result;
    }
    
    private static void updateStatus(JLabel label, JProgressBar bar, JTextArea log, 
                                     String status, int progress) {
        SwingUtilities.invokeLater(() -> {
            label.setText(status);
            bar.setValue(progress);
            log.append(status + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }
    
    private static void unzip(File zipFile, File destDir, JTextArea log) throws IOException {
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(".json", ".png", ".jpg", ".txt", ".md", ".properties"));
    
    private static boolean isAllowedFile(String name) {
        for (String ext : ALLOWED_EXT) {
            if (name.toLowerCase().endsWith(ext)) return true;
        }
        return false;
    }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File targetFile = new File(destDir, entry.getName());
                
                if (entry.isDirectory()) {
                } else if (!isAllowedFile(entry.getName())) {
                    logArea.append("跳过非法文件: " + entry.getName() + "\n");
                    continue;
                } else if (!isAllowedFile(entry.getName())) {
                    logArea.append("跳过非法文件: " + entry.getName() + "\n");
                    continue;
                    targetFile.mkdirs();
                } else {
                    targetFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                
                final String entryName = entry.getName();
                SwingUtilities.invokeLater(() -> {
                    log.append("解压: " + entryName + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                });
                
                zis.closeEntry();
            }
        }
    }
    
    private static void showImportResult(ImportResult result, JFrame parent) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Microsoft YaHei; padding: 10px;'>");
        
        if (result.success) {
            message.append("<h2 style='color: #4CAF50;'>导入成功</h2>");
            message.append("<p><b>计划名称:</b> ").append(result.planName).append("</p>");
            message.append("<p><b>框题数量:</b> ").append(result.sectionCount).append(" 个</p>");
            message.append("<p><b>题目总数:</b> ").append(result.questionCount).append(" 题</p>");
            
            if (!result.warnings.isEmpty()) {
                message.append("<p style='color: #FFA500;'><b>警告:</b></p><ul>");
                for (String w : result.warnings) {
                    message.append("<li>").append(w).append("</li>");
                }
                message.append("</ul>");
            }
        } else {
            message.append("<h2 style='color: #f44336;'>导入失败</h2>");
            for (String e : result.errors) {
                message.append("<p style='color: #ff8a80;'>").append(e).append("</p>");
            }
        }
        
        message.append("</body></html>");
        
        JOptionPane.showMessageDialog(parent, message.toString(), 
            result.success ? "导入成功" : "导入失败",
            result.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}
