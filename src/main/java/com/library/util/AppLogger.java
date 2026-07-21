package com.library.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lightweight static logger writing to console and a log file.
 */
public final class AppLogger {
    private static final Path LOG_FILE = Path.of("logs/application.log");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private AppLogger() {}
    public static void info(String tag, String message) { log("INFO", tag, message, null); }
    public static void warn(String tag, String message) { log("WARN", tag, message, null); }
    public static void error(String tag, String message) { log("ERROR", tag, message, null); }
    public static void error(String tag, String message, Throwable t) { log("ERROR", tag, message, t); }
    private static void log(String level, String tag, String message, Throwable t) {
        String line = FMT.format(LocalDateTime.now()) + " [" + level + "] " + tag + " - " + message;
        if (t != null) line += System.lineSeparator() + getStackTrace(t);
        System.out.println(line);
        try {
            Files.createDirectories(LOG_FILE.getParent());
            Files.writeString(LOG_FILE, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }
    private static String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement e : t.getStackTrace()) sb.append("\t").append(e).append(System.lineSeparator());
        return sb.toString();
    }
}
