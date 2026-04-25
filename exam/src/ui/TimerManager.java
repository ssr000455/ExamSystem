package ui;

import javax.swing.Timer;
import java.util.*;

public class TimerManager {
    private static final List<Timer> activeTimers = new ArrayList<>();
    
    public static Timer createTimer(int delay, Runnable action) {
        Timer timer = new Timer(delay, e -> action.run());
        activeTimers.add(timer);
        return timer;
    }
    
    public static void stopAll() {
        for (Timer t : activeTimers) {
            if (t.isRunning()) t.stop();
        }
        activeTimers.clear();
    }
    
    public static void removeTimer(Timer timer) {
        if (timer != null) {
            timer.stop();
            activeTimers.remove(timer);
        }
    }
}
