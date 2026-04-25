package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ChoicePanel extends JPanel {
    private ButtonGroup buttonGroup;
    private java.util.List<JRadioButton> radioButtons;
    private int selectedIndex = -1;
    private JLabel statusLabel;
    
    public ChoicePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        radioButtons = new ArrayList<>();
        buttonGroup = new ButtonGroup();
    }
    
    private Color getBackgroundColor() {
        return UIManager.getColor("Panel.background");
    }
    
    private Color getForegroundColor() {
        return UIManager.getColor("Label.foreground");
    }
    
    private Color getAccentColor() {
        Color accent = UIManager.getColor("Component.focusedBorderColor");
        if (accent == null) accent = UIManager.getColor("Button.select");
        if (accent == null) accent = new Color(75, 110, 175);
        return accent;
    }
    
    private Color getHoverColor() {
        return UIManager.getColor("MenuItem.selectionBackground");
    }
    
    public void setOptions(String[] options) {
        removeAll();
        radioButtons.clear();
        
        Color bgColor = getBackgroundColor();
        Color fgColor = getForegroundColor();
        Color accentColor = getAccentColor();
        Color hoverColor = getHoverColor();
        
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(bgColor);
        
        for (int i = 0; i < options.length; i++) {
            JRadioButton radio = new JRadioButton(options[i]);
            radio.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            radio.setBackground(bgColor);
            radio.setForeground(fgColor);
            radio.setFocusPainted(false);
            radio.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            radio.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            radio.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!radio.isSelected()) {
                        radio.setBackground(hoverColor);
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (!radio.isSelected()) {
                        radio.setBackground(bgColor);
                    }
                }
            });
            
            final int index = i;
            radio.addActionListener(e -> {
                selectedIndex = index;
                updateStatus();
            });
            
            buttonGroup.add(radio);
            radioButtons.add(radio);
            
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            rowPanel.setBackground(bgColor);
            
            JLabel letterLabel = new JLabel(String.valueOf((char)('A' + i)));
            letterLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
            letterLabel.setForeground(accentColor);
            letterLabel.setPreferredSize(new Dimension(30, 30));
            letterLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            rowPanel.add(letterLabel);
            rowPanel.add(radio);
            
            optionsPanel.add(rowPanel);
            optionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        add(new JScrollPane(optionsPanel), BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        statusLabel = new JLabel("请选择一个选项");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
        
        revalidate();
        repaint();
    }
    
    private void updateStatus() {
        if (selectedIndex >= 0) {
            statusLabel.setText("已选择: " + (char)('A' + selectedIndex));
            statusLabel.setForeground(new Color(75, 175, 80));
        }
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public void setSelectedIndex(int index) {
        if (index >= 0 && index < radioButtons.size()) {
            radioButtons.get(index).setSelected(true);
            selectedIndex = index;
            updateStatus();
        }
    }
    
    public void clearSelection() {
        buttonGroup.clearSelection();
        selectedIndex = -1;
        statusLabel.setText("请选择一个选项");
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
    }
    
    public static int showChoiceDialog(JFrame parent, String title, String[] options) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(parent);
        
        Color bgColor = UIManager.getColor("Panel.background");
        Color fgColor = UIManager.getColor("Label.foreground");
        Color accentColor = UIManager.getColor("Component.focusedBorderColor");
        if (accentColor == null) accentColor = new Color(75, 110, 175);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(bgColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(accentColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        ChoicePanel choicePanel = new ChoicePanel();
        choicePanel.setOptions(options);
        mainPanel.add(choicePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(bgColor);
        
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        cancelBtn.addActionListener(e -> {
            choicePanel.clearSelection();
            dialog.dispose();
        });
        
        JButton submitBtn = new JButton("提交答案");
        submitBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        submitBtn.setBackground(accentColor);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(submitBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        
        return choicePanel.getSelectedIndex();
    }
}
