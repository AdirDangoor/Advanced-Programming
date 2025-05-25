package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean DEBUG_MODE = true; // Can be set from Main or other classes

    public static void setDebugMode(boolean enabled) {
        DEBUG_MODE = enabled;
    }

    public static void info(String message) {
        if (DEBUG_MODE) {
            log("INFO", message);
        }
    }

    public static void warn(String message) {
        // Always log warnings regardless of debug mode
        log("WARN", message);
    }

    public static void error(String message) {
        // Always log errors regardless of debug mode
        log("ERROR", message);
    }

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] [" + level + "] " + message);
    }
}