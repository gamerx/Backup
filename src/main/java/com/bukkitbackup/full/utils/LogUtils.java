package com.bukkitbackup.full.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public class LogUtils {

    private static Plugin plugin;
    private static Logger logger;
    private static boolean logToConsole;

    public static void initLogUtils(Plugin plugin) {
        if (LogUtils.logger == null) {
            if (plugin != null) {
                LogUtils.logger = Logger.getLogger(plugin.getServer().getLogger().getName() + "." + plugin.getServer().getName());
            }
            LogUtils.plugin = plugin;
        }
    }

    public static void finishInitLogUtils(boolean logToConsole) {
        LogUtils.logToConsole = logToConsole;
    }

    public static void sendLog(String message) {
        sendLog(message, true);
    }

    public static void sendLog(String message, boolean print) {

        if (message.contains(";;")) {
            List<String> messageList = Arrays.asList(message.split(";;"));

            for (int i = 0; i < messageList.size(); i++) {
                String thisMessage = messageList.get(i);
                if (logToConsole && print) {
                    logger.log(Level.INFO, "[".concat(plugin.getDescription().getName()).concat("] ").concat(thisMessage));
                }
            }

        } else {

            if (logToConsole && print) {
                logger.log(Level.INFO, "[".concat(plugin.getDescription().getName()).concat("] ").concat(message));
            }
        }
    }

    public static void exceptionLog(Throwable ste, String message) {
        sendLog(message);
        exceptionLog(ste);
    }

    public static void exceptionLog(Throwable ste) {
        sendLog("Please provide following error with support request:");
        ste.printStackTrace(System.out);
    }

}
