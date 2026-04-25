package config;

import com.formdev.flatlaf.*;
import javax.swing.*;
import java.awt.Window;
import java.util.prefs.Preferences;
import java.io.File;

public class ConfigManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(ConfigManager.class);
    private static ConfigManager instance;
    
    public enum Theme { LIGHT, DARK }
    public enum Language { ZH_CN, EN, RU, ES, JA, PT, ZH_TW }
    public enum EditorMode { DEFAULT, VIM, NANO }
    
    private Theme theme;
    private Language language;
    private EditorMode editorMode;
    private boolean autoComplete;
    private boolean codeFolding;
    private String storagePath;
    
    private ConfigManager() { load(); }
    
    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }
    
    public void load() {
        theme = Theme.valueOf(prefs.get("theme", Theme.DARK.name()));
        language = Language.valueOf(prefs.get("language", Language.ZH_CN.name()));
        editorMode = EditorMode.valueOf(prefs.get("editorMode", EditorMode.DEFAULT.name()));
        autoComplete = prefs.getBoolean("autoComplete", true);
        codeFolding = prefs.getBoolean("codeFolding", true);
        storagePath = prefs.get("storagePath", System.getProperty("user.home") + File.separator + "ExamSystem_Files");
    }
    
    public void save() {
        prefs.put("theme", theme.name());
        prefs.put("language", language.name());
        prefs.put("editorMode", editorMode.name());
        prefs.putBoolean("autoComplete", autoComplete);
        prefs.putBoolean("codeFolding", codeFolding);
        prefs.put("storagePath", storagePath);
    }
    
    public void applyTheme() {
        SwingUtilities.invokeLater(() -> {
            try {
                LookAndFeel laf = (theme == Theme.LIGHT) ? new FlatLightLaf() : new FlatDarkLaf();
                UIManager.setLookAndFeel(laf);
                if (theme == Theme.LIGHT) {
                    UIManager.put("Component.borderColor", new javax.swing.plaf.ColorUIResource(200, 200, 200));
                }
                for (Window w : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(w);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public Theme getTheme() { return theme; }
    public void setTheme(Theme t) { this.theme = t; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language l) { this.language = l; }
    public EditorMode getEditorMode() { return editorMode; }
    public void setEditorMode(EditorMode m) { this.editorMode = m; }
    public boolean isAutoComplete() { return autoComplete; }
    public void setAutoComplete(boolean b) { this.autoComplete = b; }
    public boolean isCodeFolding() { return codeFolding; }
    public void setCodeFolding(boolean b) { this.codeFolding = b; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String p) { this.storagePath = p; }
}
