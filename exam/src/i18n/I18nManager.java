package i18n;

import config.ConfigManager;
import java.util.*;
import java.text.MessageFormat;

public class I18nManager {
    private static I18nManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;
    
    private I18nManager() {
        loadLocale();
    }
    
    public static I18nManager getInstance() {
        if (instance == null) instance = new I18nManager();
        return instance;
    }
    
    @SuppressWarnings("deprecation")
    private void loadLocale() {
        ConfigManager.Language lang = ConfigManager.getInstance().getLanguage();
        switch (lang) {
            case ZH_CN: currentLocale = Locale.SIMPLIFIED_CHINESE; break;
            case EN: currentLocale = Locale.ENGLISH; break;
            case RU: currentLocale = new Locale("ru"); break;
            case ES: currentLocale = new Locale("es"); break;
            case JA: currentLocale = Locale.JAPANESE; break;
            case PT: currentLocale = new Locale("pt"); break;
            case ZH_TW: currentLocale = Locale.TRADITIONAL_CHINESE; break;
            default: currentLocale = Locale.SIMPLIFIED_CHINESE;
        }
        try {
            bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle("i18n.messages", Locale.SIMPLIFIED_CHINESE);
        }
    }
    
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }
    
    public String get(String key, Object... args) {
        String pattern = get(key);
        if (pattern.startsWith("!") && pattern.endsWith("!")) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }
    
    public void refresh() {
        loadLocale();
    }
}
