package ui;

import config.ConfigManager;
import i18n.I18nManager;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;

public class SettingsDialog extends JDialog {
    private ConfigManager config;
    private I18nManager i18n;
    private JComboBox<String> themeCombo, langCombo, editorCombo;
    private JCheckBox autoCompleteCB, codeFoldingCB;
    private JTextField storagePathField;
    private MainFrame mainFrame;
    
    public SettingsDialog(MainFrame parent) {
        super(parent, I18nManager.getInstance().get("settings.title"), true);
        this.mainFrame = parent;
        i18n = I18nManager.getInstance();
        config = ConfigManager.getInstance();
        setSize(550, 450);
        setLocationRelativeTo(parent);
        buildUI();
    }
    
    private void buildUI() {
        getContentPane().removeAll();
        JTabbedPane tabs = new JTabbedPane();
        tabs.add(i18n.get("settings.appearance"), createAppearancePanel());
        tabs.add(i18n.get("settings.editor"), createEditorPanel());
        tabs.add(i18n.get("settings.storage"), createStoragePanel());
        add(tabs, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton(i18n.get("dialog.save"));
        saveBtn.addActionListener(e -> saveSettings());
        JButton cancelBtn = new JButton(i18n.get("dialog.cancel"));
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn); buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    private JPanel createAppearancePanel() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(5,5,5,5); g.anchor = GridBagConstraints.WEST;
        g.gridx=0; g.gridy=0; p.add(new JLabel(i18n.get("settings.theme")), g);
        themeCombo = new JComboBox<>(new String[]{i18n.get("settings.light"), i18n.get("settings.dark")});
        themeCombo.setSelectedIndex(config.getTheme() == ConfigManager.Theme.LIGHT ? 0 : 1);
        g.gridx=1; p.add(themeCombo, g);
        g.gridx=0; g.gridy=1; p.add(new JLabel(i18n.get("settings.language")), g);
        langCombo = new JComboBox<>(new String[]{"简体中文", "English", "Русский", "Español", "日本語", "Português", "繁體中文"});
        langCombo.setSelectedIndex(config.getLanguage().ordinal());
        g.gridx=1; p.add(langCombo, g);
        return p;
    }
    
    private JPanel createEditorPanel() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(5,5,5,5); g.anchor = GridBagConstraints.WEST;
        g.gridx=0; g.gridy=0; p.add(new JLabel(i18n.get("settings.editorMode")), g);
        editorCombo = new JComboBox<>(new String[]{i18n.get("settings.default"), i18n.get("settings.vim"), i18n.get("settings.nano")});
        editorCombo.setSelectedIndex(config.getEditorMode().ordinal());
        g.gridx=1; p.add(editorCombo, g);
        autoCompleteCB = new JCheckBox(i18n.get("settings.autoComplete"), config.isAutoComplete());
        g.gridx=0; g.gridy=1; g.gridwidth=2; p.add(autoCompleteCB, g);
        codeFoldingCB = new JCheckBox(i18n.get("settings.codeFolding"), config.isCodeFolding());
        g.gridy=2; p.add(codeFoldingCB, g);
        return p;
    }
    
    private JPanel createStoragePanel() {
        JPanel p = new JPanel(new BorderLayout(10,10)); p.setBorder(new EmptyBorder(10,10,10,10));
        JPanel pathPanel = new JPanel(new BorderLayout(5,0));
        storagePathField = new JTextField(config.getStoragePath());
        JButton browseBtn = new JButton(i18n.get("settings.browse"));
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                storagePathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        pathPanel.add(storagePathField, BorderLayout.CENTER); pathPanel.add(browseBtn, BorderLayout.EAST);
        p.add(new JLabel(i18n.get("settings.storagePath")), BorderLayout.NORTH);
        p.add(pathPanel, BorderLayout.CENTER);
        return p;
    }
    
    private void saveSettings() {
        config.setTheme(themeCombo.getSelectedIndex() == 0 ? ConfigManager.Theme.LIGHT : ConfigManager.Theme.DARK);
        config.setLanguage(ConfigManager.Language.values()[langCombo.getSelectedIndex()]);
        config.setEditorMode(ConfigManager.EditorMode.values()[editorCombo.getSelectedIndex()]);
        config.setAutoComplete(autoCompleteCB.isSelected());
        config.setCodeFolding(codeFoldingCB.isSelected());
        config.setStoragePath(storagePathField.getText());
        config.save();
        config.applyTheme();
        i18n.refresh();
        
        // 刷新主窗口 UI（无重启，瞬间完成）
        mainFrame.fullRefresh();
        mainFrame.editorPanel.refreshSettings();
        
        dispose();
        JOptionPane.showMessageDialog(mainFrame, i18n.get("settings.restart"), i18n.get("settings.restartTitle"), JOptionPane.INFORMATION_MESSAGE);
    }
}
