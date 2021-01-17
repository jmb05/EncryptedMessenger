package net.jmb19905.messenger.util;

import com.esotericsoftware.minlog.Log;
import com.sun.istack.internal.Nullable;

import java.io.*;
import java.util.Date;

public class EMLogger extends Log.Logger {

    public static File logFile;
    private static BufferedWriter writer;
    private final long firstLogTime = new Date().getTime();

    private static EMLogger instance;

    public static final int LEVEL_NONE = 6;
    public static final int LEVEL_ERROR = 5;
    public static final int LEVEL_WARN = 4;
    public static final int LEVEL_INFO = 3;
    public static final int LEVEL_DEBUG = 2;
    public static final int LEVEL_TRACE = 1;

    public static boolean ERROR = true;
    public static boolean WARN = true;
    public static boolean INFO = true;
    public static boolean DEBUG = true;
    public static boolean TRACE = false;

    /**
     * Initializes the Logger
     *  -> creates Log file
     *  -> initializes the File Writer (BufferedWriter)
     */
    public static void init() {
        logFile = new File("logs/" + Variables.currentTime + "_" + Variables.currentSide + ".log");
        if (!Util.createFile(logFile)) {
            Log.error("EMLogger", "Error creating log file");
            return;
        }
        try {
            writer = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            Log.error("EMLogger", "Error creating File Writer for the Log File", e);
        }
        instance = new EMLogger();
    }

    /**
     * Closes the File Writer (BufferedWriter) for the Log File
     */
    public static void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                Log.error("EMLogger", "Error closing file writer", e);
            }
        }
    }

    /**
     * Logs a message to the console and the Log File
     * @param level the level of the Log Message
     * @param category the category of the Log message
     * @param message the message
     * @param ex an optional Exception
     */
    @Override
    public void log(int level, String category, String message, @Nullable Throwable ex) {
        //Basically copied code from the original method
        StringBuilder builder = new StringBuilder(256);

        long time = new Date().getTime() - firstLogTime;
        long minutes = time / (1000 * 60);
        long seconds = time / (1000) % 60;
        if (minutes <= 9) builder.append('0');
        builder.append(minutes);
        builder.append(':');
        if (seconds <= 9) builder.append('0');
        builder.append(seconds);

        switch (level) {
            case Log.LEVEL_ERROR:
                builder.append(" ERROR: ");
                break;
            case Log.LEVEL_WARN:
                builder.append("  WARN: ");
                break;
            case Log.LEVEL_INFO:
                builder.append("  INFO: ");
                break;
            case Log.LEVEL_DEBUG:
                builder.append(" DEBUG: ");
                break;
            case Log.LEVEL_TRACE:
                builder.append(" TRACE: ");
                break;
        }

        if (category != null) {
            builder.append('[');
            builder.append(category);
            builder.append("] ");
        }

        builder.append(message);

        if (ex != null) {
            StringWriter writer = new StringWriter(256);
            ex.printStackTrace(new PrintWriter(writer));
            builder.append('\n');
            builder.append(writer.toString().trim());
        }

        print(builder.toString());
        if (logFile.exists() && writer != null) {
            try {
                writer.write(builder.toString() + "\n");
                writer.flush();
            } catch (IOException e) {
                Log.error("EMLogger", "Error writing to log file", e);
            }
        }
    }

    /**
     * Sets the level for the custom Logger
     * if the level of a message is lower than the level of the Logger the message will not be logged
     * @param level the level of the Logger
     */
    public static void setLevel(int level) {
        EMLogger.ERROR = level <= LEVEL_ERROR;
        EMLogger.WARN = level <= LEVEL_WARN;
        EMLogger.INFO = level <= LEVEL_INFO;
        EMLogger.DEBUG = level <= LEVEL_DEBUG;
        EMLogger.TRACE = level <= LEVEL_TRACE;
    }

    /**
     * Logs a Message with the level TRACE
     * @param category the category of the Log message
     * @param message the message
     */
    public static void trace(String category, String message) {
        if (TRACE)
            instance.log(Log.LEVEL_TRACE, category, message, null);
    }

    /**
     * Logs a Message with the level TRACE
     * @param category the category of the Log message
     * @param message the message
     * @param ex an optional Exception
     */
    public static void trace(String category, String message, Throwable ex) {
        if (TRACE)
            instance.log(Log.LEVEL_TRACE, category, message, ex);
    }

    /**
     * Logs a Message with the level DEBUG
     * @param category the category of the Log message
     * @param message the message
     */
    public static void debug(String category, String message) {
        if (DEBUG)
            instance.log(Log.LEVEL_DEBUG, category, message, null);
    }

    /**
     * Logs a Message with the level DEBUG
     * @param category the category of the Log message
     * @param message the message
     * @param ex an optional Exception
     */
    public static void debug(String category, String message, Throwable ex) {
        if (DEBUG)
            instance.log(Log.LEVEL_DEBUG, category, message, ex);
    }

    /**
     * Logs a Message with the level INFO
     * @param category the category of the Log message
     * @param message the message
     */
    public static void info(String category, String message) {
        if (INFO)
            instance.log(Log.LEVEL_INFO, category, message, null);
    }

    /**
     * Logs a Message with the level INFO
     * @param category the category of the Log message
     * @param message the message
     * @param ex an optional Exception
     */
    public static void info(String category, String message, Throwable ex) {
        if (INFO)
            instance.log(Log.LEVEL_INFO, category, message, ex);
    }

    /**
     * Logs a Message with the level WARN
     * @param category the category of the Log message
     * @param message the message
     */
    public static void warn(String category, String message) {
        if (WARN)
            instance.log(Log.LEVEL_WARN, category, message, null);
    }

    /**
     * Logs a Message with the level WARN
     * @param category the category of the Log message
     * @param message the message
     * @param ex an optional Exception
     */
    public static void warn(String category, String message, Throwable ex) {
        if (WARN)
            instance.log(Log.LEVEL_WARN, category, message, ex);
    }

    /**
     * Logs a Message with the level ERROR
     * @param category the category of the Log message
     * @param message the message
     */
    public static void error(String category, String message) {
        if (ERROR)
            instance.log(Log.LEVEL_ERROR, category, message, null);
    }

    /**
     * Logs a Message with the level ERROR
     * @param category the category of the Log message
     * @param message the message
     * @param ex an optional Exception
     */
    public static void error(String category, String message, Throwable ex) {
        if (ERROR)
            instance.log(Log.LEVEL_ERROR, category, message, ex);
    }

}
