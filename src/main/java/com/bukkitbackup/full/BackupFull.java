package com.bukkitbackup.full;

import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.config.UpdateChecker;
import com.bukkitbackup.full.events.CommandHandler;
import com.bukkitbackup.full.events.EventListener;
import com.bukkitbackup.full.threading.BackupScheduler;
import com.bukkitbackup.full.threading.BackupTask;
import com.bukkitbackup.full.threading.PrepareBackup;
import com.bukkitbackup.full.threading.tasks.BackupEverything;
import com.bukkitbackup.full.threading.tasks.BackupPlugins;
import com.bukkitbackup.full.threading.tasks.BackupWorlds;
import com.bukkitbackup.full.utils.FileUtils;
import com.bukkitbackup.full.utils.LogUtils;
import com.bukkitbackup.full.utils.MetricUtils;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Backup - The simple server backup solution.
 *
 * @author Domenic Horner (gamerx)
 */
public class BackupFull extends JavaPlugin {

    // Public variables for class comms.
    private static PrepareBackup prepareBackup;
    public static BackupTask backupTask;
    public static BackupWorlds backupWorlds;
    public static BackupPlugins backupPlugins;
    public static BackupEverything backupEverything;
    // Private variables for this class.
    private static Settings settings;
    private static Strings strings;
    private File thisDataFolder;
    private String clientUID;

    @Override
    public void onLoad() {

        // Set Data Folder, Init log utils.
        thisDataFolder = this.getDataFolder();
        LogUtils.initLogUtils(this);
        FileUtils.checkFolderAndCreate(thisDataFolder);

        // Setup Configuration Files.
        strings = new Strings(new File(thisDataFolder, "strings.yml"));
        settings = new Settings(new File(thisDataFolder, "config.yml"), strings);

        // Run version checking on configurations.
        //@TODO Refactor Settings & Strings Loading Code.
        strings.checkStringsVersion(settings.getStringProperty("version", ""));
        settings.checkSettingsVersion(this.getDescription().getVersion());

        // Complete loading log utils.
        LogUtils.finishInitLogUtils(settings.getBooleanProperty("displaylog", true), settings.getBooleanProperty("debugenabled", false));

        // BukkitMetrics Loading. (Not Plugin-Specific)
        try {
            MetricUtils metricUtils = new MetricUtils(this);
            metricUtils.start();
            clientUID = metricUtils.guid;
        } catch (IOException ex) {
            LogUtils.exceptionLog(ex, "Exception loading metrics.");
        }

    }

    @Override
    public void onEnable() {

        // Get server and plugin manager instances.
        Server pluginServer = getServer();
        PluginManager pluginManager = pluginServer.getPluginManager();

        // Check backup path, create if required.
        FileUtils.checkFolderAndCreate(new File(settings.getStringProperty("backuppath", "backups")));

        // Setup backup tasks.
        backupTask = new BackupTask(this, settings, strings);
        backupWorlds = new BackupWorlds(pluginServer, settings, strings);
        backupPlugins = new BackupPlugins(settings, strings);
        backupEverything = new BackupEverything(settings);

        // Create new "PrepareBackup" instance.
        prepareBackup = new PrepareBackup(this, settings, strings);

        // Initalize Command Listener.
        getCommand("backup").setExecutor(new CommandHandler(prepareBackup, this, settings, strings));
        getCommand("bu").setExecutor(new CommandHandler(prepareBackup, this, settings, strings));

        // Initalize Event Listener.
        EventListener eventListener = new EventListener(prepareBackup, this, settings, strings);
        pluginManager.registerEvents(eventListener, this);

        // Get the backup interval setting, and clean it.
        String backupInterval = settings.getStringProperty("backupinterval", "15M").trim().toLowerCase();

        // Initalize default variables.
        int backupMinutes = 0; // Should contain interval, in minutes.
        String[] backupSchedArray = null; // Should contain an array of times.

        // Matches one or more numbers. (Interpret them as minutes)
        if (backupInterval.matches("^[0-9]+$")) {

            // Parse the value to integer.
            backupMinutes = Integer.parseInt(backupInterval);
            LogUtils.sendDebug("Entry is set to minutes. (M:0002)");

        } // Matches one or more numbers, followed by a letter.
        else if (backupInterval.matches("[0-9]+[a-z]")) {

            // Parse for the integer.
            Pattern timePattern = Pattern.compile("^([0-9]+)[a-z]$");
            Matcher amountTime = timePattern.matcher(backupInterval);

            // Parse it for the letter.
            Pattern letterPattern = Pattern.compile("^[0-9]+([a-z])$");
            Matcher letterTime = letterPattern.matcher(backupInterval);

            // Confirm that we found a match for both items.
            if (letterTime.matches() && amountTime.matches()) {

                // Assign values to the variables.
                String letter = letterTime.group(1);
                int time = Integer.parseInt(amountTime.group(1));

                // Perform matching for time spans, calculate back to minutes.
                if (letter.equals("m")) { // Minutes
                    backupMinutes = time;
                } else if (letter.equals("h")) { // Hours
                    backupMinutes = time * 60;
                } else if (letter.equals("d")) { // Days
                    backupMinutes = time * 60 * 12;
                } else if (letter.equals("w")) { // Weeks
                    backupMinutes = time * 60 * 12 * 7;
                } else { // Assume minutes.
                    LogUtils.sendLog(strings.getString("unknowntimeident"));
                    backupMinutes = time;
                }
            } else {
                LogUtils.sendLog(strings.getString("checkbackupinterval"));
            }
            LogUtils.sendDebug("Found correctly-formatted time (M:0001)");

        } // Matches "TA[02:00,06:00,10:00,14:00,18:00,22:00]", or similar.
        else if (backupInterval.matches("^ta\\[(.*)\\]$")) {

            // Parse the string to get the array.
            Pattern letterPattern = Pattern.compile("^ta\\[(.*)\\]$");
            Matcher array = letterPattern.matcher(backupInterval);

            // Put the array into a variable.
            backupSchedArray = array.toString().split(",");
            LogUtils.sendDebug("Found time array string. (M:0003)");

        } else {

            // Nothing found.
            LogUtils.sendLog(strings.getString("checkbackupinterval"));
            backupMinutes = 0;
            LogUtils.sendDebug("No correct backup interval string found. (M:0004)");

        }

        // If interval is defined.
        if (backupMinutes != 0) {

            // Convert to server ticks.
            int backupIntervalInTicks = (backupMinutes * 1200);

            // Schedule a repeating backup task.
            pluginServer.getScheduler().scheduleAsyncRepeatingTask(this, prepareBackup, backupIntervalInTicks, backupIntervalInTicks);

            LogUtils.sendDebug("Doing recurring backup interval code. (M:0005)");

        } // If the backup should be done at pre-defined times.
        else if (backupSchedArray != null) {

            // Create a backup scheduler instance.
            BackupScheduler backupScheduler = new BackupScheduler(this, prepareBackup, settings, strings, backupSchedArray);

            // Start the scheduler as another thread.
            pluginServer.getScheduler().scheduleAsyncDelayedTask(this, backupScheduler);

            LogUtils.sendDebug("Doing time array backup code. (M:0006)");

        } // Automatic backups must be disabled.
        else {

            // Alert the user of disabled backup.
            LogUtils.sendLog(strings.getString("disbaledauto"));

            LogUtils.sendDebug("Disabled automatic backup. (M:0007)");

        }

        // If the update check is enabled.
        if (settings.getBooleanProperty("enableversioncheck", true)) {

            // Start the update checker in another thread.
            pluginServer.getScheduler().scheduleAsyncDelayedTask(this, new UpdateChecker(this.getDescription(), strings, clientUID));
        }

        // Notify loading complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " enabled!");
    }

    @Override
    public void onDisable() {

        // Stop any scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);

        // Shutdown complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " diabled!");
    }
}
