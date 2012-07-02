package com.bukkitbackup.full.threading;

import com.bukkitbackup.full.BackupFull;
import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.utils.LogUtils;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PrepareBackup implements Runnable {

    public boolean isLastBackup;
    public boolean isManualBackup;
    public boolean backupEnabled;
    public LinkedList<String> worldsToBackup;
    private final Server server;
    private final Settings settings;
    private Strings strings;
    private Plugin plugin;

    public PrepareBackup(Server server, Settings settings, Strings strings) {
        this.server = server;
        this.settings = settings;
        this.plugin = server.getPluginManager().getPlugin("Backup");
        this.strings = strings;
        isLastBackup = false;
        backupEnabled = true;
    }

    @Override
    public synchronized void run() {
        if (backupEnabled) {
            checkShouldDoBackup();
        } else {
            LogUtils.sendLog(strings.getString("backupoff"));
        }
    }

    /**
     * This method decides whether the doBackup should be run.
     *
     * It checks: - Online players. - Bypass node. - Manual doBackup.
     *
     * It then runs the doBackup if needed.
     */
    private void checkShouldDoBackup() {

        // If it is a manual doBackup, start it, otherwise, perform checks.
        if (isManualBackup) {
            prepareBackup();
        } else {

            // No player checking.
            if (settings.getBooleanProperty("backupemptyserver")) {
                prepareBackup();
            } else {

                // Checking online players.
                if (server.getOnlinePlayers().length == 0) {

                    // Check if last backup
                    if (isLastBackup) {
                        LogUtils.sendLog(strings.getString("lastbackup"));
                        prepareBackup();
                        isLastBackup = false;
                    } else {
                        LogUtils.sendLog(strings.getString("abortedbackup", Integer.toString(settings.getIntervalInMinutes("backupinterval"))));
                    }
                } else {

                    // Default don't do backup.
                    boolean doBackup = false;

                    // Get all online players.
                    Player[] players = server.getOnlinePlayers();

                    // Loop players.
                    for (int player = 0; player < players.length; player++) {
                        Player currentplayer = players[player];

                        // If any players do not have the node, do the doBackup.
                        if (!currentplayer.hasPermission("backup.bypass")) {
                            doBackup = true;
                        }
                    }

                    // Final check if we should do the backup.
                    if (doBackup) {
                        prepareBackup();
                    } else {
                        LogUtils.sendLog(strings.getString("skipbackupbypass"));
                    }
                }
            }
        }

        // Check we should do a save-all.
        if (settings.getBooleanProperty("alwayssaveall")) {
            server.getScheduler().scheduleSyncDelayedTask(plugin, new SyncSaveAll(server, 0));
            LogUtils.sendLog(strings.getString("alwayssaveall"));
        }
    }

    /**
     * Prepared for, and starts, a doBackup.
     */
    protected void prepareBackup() {

        // Notify doBackup has started.
        notifyStarted();

        // Perform final world save before backup, then turn off auto-saving.
        server.getScheduler().scheduleSyncDelayedTask(plugin, new SyncSaveAll(server, 1));

        // Save all players.
        server.savePlayers();



        // Scedule the doBackup.
        server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                server.getScheduler().scheduleAsyncDelayedTask(plugin, BackupFull.backupTask);
            }
        });
        isManualBackup = false;
    }

    /**
     * Notify that the backup has started.
     *
     */
    private void notifyStarted() {

        // Get message.
        String startBackupMessage = strings.getString("backupstarted");

        // Check the string is set.
        if (startBackupMessage != null && !startBackupMessage.trim().isEmpty()) {

            // Check if we are using multiple lines.
            if (startBackupMessage.contains(";;")) {

                // Convert to array of lines.
                List<String> messageList = Arrays.asList(startBackupMessage.split(";;"));

                // Loop the lines of this message.
                for (int i = 0; i < messageList.size(); i++) {

                    // Retrieve this line of the message.
                    String thisMessage = messageList.get(i);

                    // Notify all players, regardless of the permission node.
                    if (settings.getBooleanProperty("notifyallplayers")) {
                        server.broadcastMessage(thisMessage);
                    } else {

                        // Get all players.
                        Player[] players = server.getOnlinePlayers();

                        // Loop through all online players.
                        for (int pos = 0; pos < players.length; pos++) {
                            Player currentplayer = players[pos];

                            // If the current player has the right permissions, notify them.
                            if (currentplayer.hasPermission("backup.notify")) {
                                currentplayer.sendMessage(thisMessage);
                            }
                        }
                    }
                    LogUtils.sendLog(thisMessage, false);
                }

            } else {

                // Notify all players, regardless of the permission node.
                if (settings.getBooleanProperty("notifyallplayers")) {
                    server.broadcastMessage(startBackupMessage);
                } else {

                    // Get all players.
                    Player[] players = server.getOnlinePlayers();

                    // Loop through all online players.
                    for (int pos = 0; pos < players.length; pos++) {
                        Player currentplayer = players[pos];

                        // If the current player has the right permissions, notify them.
                        if (currentplayer.hasPermission("backup.notify")) {
                            currentplayer.sendMessage(startBackupMessage);
                        }
                    }
                }
                LogUtils.sendLog(startBackupMessage, false);
            }
        }
    }

    /**
     * Set the doBackup as a manual doBackup. IE: Not scheduled.
     */
    public void setAsManualBackup() {
        this.isManualBackup = true;
    }

    /**
     * Set the doBackup as a last doBackup.
     */
    public void setAsLastBackup(boolean isLast) {
        this.isLastBackup = isLast;
    }
}
