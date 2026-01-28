package cn.pupperclient.skimca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A centralized logger for the Skimca module.
 * Provides formatted logging methods with prefixes for different log levels.
 */
public class SkimcaLogger {

    /** The underlying Log4j logger instance. */
    private static final Logger logger = LogManager.getLogger("Skimca");

    /**
     * Logs an informational message.
     *
     * @param prefix  a prefix identifying the source component
     * @param message the message to log
     */
    public static void info(String prefix, String message) {
        logger.info("[Skimca/INFO] [" + prefix + "] " + message);
    }

    /**
     * Logs a warning message.
     *
     * @param prefix  a prefix identifying the source component
     * @param message the message to log
     */
    public static void warn(String prefix, String message) {
        logger.warn("[Skimca/WARN] [" + prefix + "] " + message);
    }

    /**
     * Logs an error message without an exception.
     *
     * @param prefix  a prefix identifying the source component
     * @param message the message to log
     */
    public static void error(String prefix, String message) {
        logger.error("[Skimca/ERROR] [" + prefix + "] " + message);
    }

    /**
     * Logs an error message with an associated exception.
     *
     * @param prefix  a prefix identifying the source component
     * @param message the message to log
     * @param e       the exception to log
     */
    public static void error(String prefix, String message, Exception e) {
        logger.error("[SC/ERROR] [" + prefix + "] " + message, e);
    }

    /**
     * Returns the underlying Log4j logger instance.
     *
     * @return the logger instance
     */
    public static Logger getLogger() {
        return logger;
    }
}