package ui;

import javax.swing.*;
import java.awt.*;

public class UIUtils {
    
    public static void fadeTransition(JComponent component, Runnable onComplete) {
        Timer timer = new Timer(16, null);
        final float[] alpha = {0.0f};
        
        timer.addActionListener(e -> {
            alpha[0] += 0.1f;
            if (alpha[0] >= 1.0f) {
                alpha[0] = 1.0f;
                timer.stop();
                if (onComplete != null) onComplete.run();
            }
            component.repaint();
        });
        timer.start();
    }
    
    public static Color blend(Color c1, Color c2, float ratio) {
        int r = (int)(c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int)(c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int)(c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }
}
