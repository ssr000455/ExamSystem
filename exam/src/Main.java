import ui.MainFrame;
import config.ConfigManager;
import i18n.I18nManager;
import javax.swing.*;
import java.util.logging.*;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在清理资源...");
            ui.TimerManager.stopAll();
            render.ModelCache.clear();
            data.QuestionLoader.clearCache();
            render.ScenePackageLoader.clearCache();
        }));
    }
    
    public static void main(String[] args) {
        parseArgs(args);
        
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOGGER.log(Level.SEVERE, "未捕获的异常: " + t.getName(), e);
            JOptionPane.showMessageDialog(null, 
                "程序发生错误: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        });
        
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                LOGGER.info("ExamSystem 启动成功");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "启动失败", e);
                JOptionPane.showMessageDialog(null, 
                    "启动失败: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--theme":
                    if (i + 1 < args.length) {
                        String theme = args[++i];
                        ConfigManager.getInstance().setTheme(
                            "浅色".equals(theme) ? ConfigManager.Theme.LIGHT : ConfigManager.Theme.DARK
                        );
                    }
                    break;
                case "--lang":
                    if (i + 1 < args.length) {
                        String lang = args[++i];
                        ConfigManager.Language language = ConfigManager.Language.ZH_CN;
                        if ("English".equals(lang)) language = ConfigManager.Language.EN;
                        ConfigManager.getInstance().setLanguage(language);
                    }
                    break;
            }
        }
        ConfigManager.getInstance().save();
        ConfigManager.getInstance().applyTheme();
        I18nManager.getInstance().refresh();
    }
}
