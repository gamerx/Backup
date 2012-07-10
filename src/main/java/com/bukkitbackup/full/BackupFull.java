package com.bukkitbackup.full;

import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.config.UpdateChecker;
import com.bukkitbackup.full.events.CommandHandler;
import com.bukkitbackup.full.events.EventListener;
import com.bukkitbackup.full.threading.BackupTask;
import com.bukkitbackup.full.threading.PrepareBackup;
import com.bukkitbackup.full.threading.SyncSaveAll;
import com.bukkitbackup.full.threading.tasks.BackupEverything;
import com.bukkitbackup.full.threading.tasks.BackupPlugins;
import com.bukkitbackup.full.threading.tasks.BackupWorlds;
import com.bukkitbackup.full.utils.FileUtils;
import com.bukkitbackup.full.utils.LogUtils;
import com.bukkitbackup.full.utils.MetricUtils;
import java.io.File;
import java.io.IOException;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * BackupFull - Plugin Loader Class.
 * This extends Bukkit's JavaPlugin class.
 *
 * @author Domenic Horner
 */
public class BackupFull extends JavaPlugin {

    public File mainDataFolder;
    private String clientID;
    private static Strings strings;
    private static Settings settings;
    private PrepareBackup prepareBackup;
    private static SyncSaveAll syncSaveAllUtil;
    private static UpdateChecker updateChecker;
    public static BackupEverything backupEverything;
    public static BackupWorlds backupWorlds;
    public static BackupPlugins backupPlugins;
    public static BackupTask backupTask;
    
    @Override
    public void onLoad() {

        // Initalize main data folder variable.
        mainDataFolder = this.getDataFolder();

        // Initalize logging utilities.
        LogUtils.initLogUtils(this);

        // check and create main datafile.
        FileUtils.checkFolderAndCreate(mainDataFolder);

        // Load configuration files.
        strings = new Strings(new File(mainDataFolder, "strings.yml"));
        settings = new Settings(this, strings, new File(mainDataFolder, "config.yml"));

        // Run version checking on strings file.
        strings.checkStringsVersion(settings.getStringProperty("requiredstrings", ""));

        // Complete initalization of LogUtils.
        LogUtils.finishInitLogUtils(settings.getBooleanProperty("displaylog", true));

        // Load Metric Utils.
        try {
            MetricUtils metricUtils = new MetricUtils(this, new File(mainDataFolder, "metrics.yml"));
            metricUtils.start();
            clientID = metricUtils.guid;
        } catch (IOException ex) {
            LogUtils.exceptionLog(ex, "Exception loading metrics.");
        }
    }

    @Override
    public void onEnable() {

        // Get server and plugin manager instances.
        Server pluginServer = getServer();
        PluginManager pluginManager = pluginServer.getPluginManager();

        // Check backup path.
        FileUtils.checkFolderAndCreate(new File(settings.getStringProperty("backuppath", "backups")));
        
        // Setup backup tasks.
        backupEverything = new BackupEverything(settings, strings);
        backupWorlds = new BackupWorlds(pluginServer, settings, strings);
        backupPlugins = new BackupPlugins(settings, strings);
        backupTask = new BackupTask(this, settings, strings);

        // Create new "PrepareBackup" instance.
        prepareBackup = new PrepareBackup(pluginServer, settings, strings);

        // Initalize the update checker code.
        updateChecker = new UpdateChecker(this.getDescription(), strings, clientID);

        // Initalize Command Listener.
        getCommand("backup").setExecutor(new CommandHandler(prepareBackup, this, settings, strings, updateChecker));
        getCommand("bu").setExecutor(new CommandHandler(prepareBackup, this, settings, strings, updateChecker));

        // Initalize Event Listener.
        EventListener eventListener = new EventListener(prepareBackup, this, settings, strings);
        pluginManager.registerEvents(eventListener, this);

        // Configure main backup task schedule.
        int backupInterval = settings.getIntervalInMinutes("backupinterval");
        if (backupInterval != -1 && backupInterval != 0) {

            // Convert to server ticks.
            int backupIntervalInTicks = (backupInterval * 1200);

            // Should the schedule repeat?
            if (settings.getBooleanProperty("norepeat", false)) {
                pluginServer.getScheduler().scheduleAsyncDelayedTask(this, prepareBackup, backupIntervalInTicks);
                LogUtils.sendLog(strings.getString("norepeatenabled", Integer.toString(backupInterval)));
            } else {
                pluginServer.getScheduler().scheduleAsyncRepeatingTask(this, prepareBackup, backupIntervalInTicks, backupIntervalInTicks);
            }
        } else {
            LogUtils.sendLog(strings.getString("disbaledauto"));
        }

        // Configure save-all schedule.
        int saveAllInterval = settings.getIntervalInMinutes("saveallinterval");
        if (saveAllInterval != 0 && saveAllInterval != -1) {

            // Convert to server ticks.
            int saveAllIntervalInTicks = (saveAllInterval * 1200);

            LogUtils.sendLog(strings.getString("savealltimeron", Integer.toString(saveAllInterval)));

            // Syncronised save-all.
            syncSaveAllUtil = new SyncSaveAll(pluginServer, 0);
            pluginServer.getScheduler().scheduleSyncRepeatingTask(this, syncSaveAllUtil, saveAllIntervalInTicks, saveAllIntervalInTicks);
        }

        // Update & version checking loading.
        if (settings.getBooleanProperty("enableversioncheck", true)) {
            pluginServer.getScheduler().scheduleAsyncDelayedTask(this, updateChecker);
        }

        // Notify loading complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!");
    }

    @Override
    public void onDisable() {

        // Stop and scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);

        // Shutdown complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completely un-loaded!");
    }
}
