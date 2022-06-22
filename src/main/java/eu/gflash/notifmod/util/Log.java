package eu.gflash.notifmod.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    public static final Logger LOGGER = LogManager.getFormatterLogger("NotifMod");

    public static void debug(String msg){
        LOGGER.debug(wTag(msg));
    }

    public static void info(String msg){
        LOGGER.info(wTag(msg));
    }

    public static void warn(String msg){
        LOGGER.warn(wTag(msg));
    }

    public static void error(String msg){
        error(msg, null);
    }

    public static void error(String msg, Throwable throwable){
        LOGGER.error(wTag(msg), throwable);
    }

    private static String wTag(String msg){
        return "[" + LOGGER.getName() + "] " + msg;
    }
}
