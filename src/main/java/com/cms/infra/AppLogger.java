package com.cms.infra;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static Level currentLevel = Level.INFO;
    private static boolean consoleEnabled = true;
    private static boolean fileEnabled = true;

    static {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }

    private AppLogger() {
    }

    public static void setLevel(Level level) {
        currentLevel = level;
    }

    public static void setConsoleEnabled(boolean enabled) {
        consoleEnabled = enabled;
    }

    public static void setFileEnabled(boolean enabled) {
        fileEnabled = enabled;
    }

    public static void debug(String message) {
        log(Level.DEBUG, getCallerInfo(), message, null);
    }

    public static void debug(String message, Object... args) {
        log(Level.DEBUG, getCallerInfo(), String.format(message, args), null);
    }

    public static void info(String message) {
        log(Level.INFO, getCallerInfo(), message, null);
    }

    public static void info(String message, Object... args) {
        log(Level.INFO, getCallerInfo(), String.format(message, args), null);
    }

    public static void warn(String message) {
        log(Level.WARN, getCallerInfo(), message, null);
    }

    public static void warn(String message, Throwable t) {
        log(Level.WARN, getCallerInfo(), message, t);
    }

    public static void error(String message) {
        log(Level.ERROR, getCallerInfo(), message, null);
    }

    public static void error(String message, Throwable t) {
        log(Level.ERROR, getCallerInfo(), message, t);
    }

    private static void log(Level level, String caller, String message, Throwable t) {
        if (level.ordinal() < currentLevel.ordinal()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(TIME_FORMAT);
        String logLine = String.format("[%s] [%s] [%s] - %s", timestamp, level, caller, message);

        if (t != null) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            logLine += "\n" + sw.toString();
        }

        if (consoleEnabled) {
            if (level == Level.ERROR) {
                System.err.println(logLine);
            } else {
                System.out.println(logLine);
            }
        }

        if (fileEnabled) {
            writeToFile(logLine, now);
        }
    }

    private static void writeToFile(String logLine, LocalDateTime time) {
        try {
            String fileName = "cms-" + time.format(DATE_FORMAT) + ".log";
            Path logFile = Paths.get(LOG_DIR, fileName);
            Files.writeString(logFile, logLine + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    private static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            if (!className.equals(AppLogger.class.getName())) {
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                return simpleClassName + "." + element.getMethodName() + ":" + element.getLineNumber();
            }
        }
        return "Unknown";
    }
}
