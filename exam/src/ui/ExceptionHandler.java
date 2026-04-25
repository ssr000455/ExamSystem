package ui;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler {
    
    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> showErrorDialog(e));
        });
    }
    
    private static void showErrorDialog(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        
        JTextArea textArea = new JTextArea(stackTrace, 20, 60);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        JOptionPane.showMessageDialog(null, scrollPane, 
            "程序发生未处理的异常", JOptionPane.ERROR_MESSAGE);
    }
}
