package ui;

import javax.swing.*;
import java.awt.*;

public class ProgressDialog extends JDialog {
    private JProgressBar progressBar;
    private JLabel label;
    
    public ProgressDialog(Frame owner, String title, String message) {
        super(owner, title, true);
        setSize(300, 120);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        label = new JLabel(message);
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        add(panel);
    }
    
    public void setMessage(String msg) {
        label.setText(msg);
    }
}
