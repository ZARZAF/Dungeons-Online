package logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Logger {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Logger.class.getName());
    private static final String ERROR_LOG_FILE_PATH = "logs/error.log";
    private static FileHandler fileHandlerErrors;
    private static final String GAME_LOG_FILE_PATH = "logs/game.log";
    private static FileHandler fileHandlerGameLogs;

    static {
        try {
            fileHandlerErrors = new FileHandler(ERROR_LOG_FILE_PATH);
            fileHandlerGameLogs = new FileHandler(GAME_LOG_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("ErrorHandler could not setup the error file.");
        }
    }

    public static void logError(FileHandler fileHandler, Level level, String errorMessage) {
        for (Handler handler : LOGGER.getHandlers()) {
            LOGGER.removeHandler(handler);
        }
        LOGGER.addHandler(fileHandler);
        LOGGER.log(level, errorMessage);
    }

    public static void logError(FileHandler fileHandler, Level level, String errorMessage, Exception e) {
        for (Handler handler : LOGGER.getHandlers()) {
            LOGGER.removeHandler(handler);
        }
        LOGGER.addHandler(fileHandler);
        LOGGER.log(level, errorMessage + getStackTraceString(e));
    }

    private static String getStackTraceString(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        StringBuilder stackTraceString = new StringBuilder();
        for (StackTraceElement s : stackTrace) {
            stackTraceString.append(s.toString());
        }

        return stackTraceString.toString();
    }

    public static FileHandler error() {
        return fileHandlerErrors;
    }

    public static FileHandler game() {
        return fileHandlerGameLogs;
    }

}
