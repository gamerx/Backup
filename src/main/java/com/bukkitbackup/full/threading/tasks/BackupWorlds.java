package com.bukkitbackup.full.threading.tasks;

import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.utils.FileUtils;
import static com.bukkitbackup.full.utils.FileUtils.FILE_SEPARATOR;
import com.bukkitbackup.full.utils.LogUtils;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.World;

/**
 * [Backup] BackupWorlds.java (World Backup) Backup worlds when the function
 * doWorlds() is called.
 *
 * @author Domenic Horner
 */
public class BackupWorlds {

    private final Server pluginServer;
    private final Settings settings;
    private final Strings strings;

    private final String worldContainer;
    private final String backupPath;
    private final LinkedList<String> worldsToBackup;
    private final boolean useTemp;
    private final boolean shouldZIP;
    private final boolean splitBackup;
    private final String tempDestination;
    private String thisTempDestination;
    

    /**
     * This should be the place where all the settings and paths for the backup
     * are defined.
     *
     * @param plugin
     * @param settings
     * @param strings
     */
    public BackupWorlds(Server server, Settings settings, Strings strings) {

        this.pluginServer = server;
        this.settings = settings;
        this.strings = strings;

        // Create list of worlds we need to backup.
        worldsToBackup = new LinkedList<String>();
        List<String> ignoredWorlds = getIgnoredWorldNames();

        for (World loopWorld : pluginServer.getWorlds()) {
            if ((loopWorld.getName() != null) && !loopWorld.getName().isEmpty() && (!ignoredWorlds.contains(loopWorld.getName()))) {
                worldsToBackup.add(loopWorld.getName());
            }
        }

        // Alert the user.
        if (worldsToBackup == null) {
            LogUtils.sendLog(strings.getString("noworlds"));
        }

        // Build folder paths.
        worldContainer = pluginServer.getWorldContainer().getName();

        // Get backup properties.
        backupPath = settings.getStringProperty("backuppath");
        shouldZIP = settings.getBooleanProperty("zipbackup");
        splitBackup = settings.getBooleanProperty("splitbackup");
        useTemp = settings.getBooleanProperty("usetemp");

        // Generate the worldStore.
        if (useTemp) {
            if (!settings.getStringProperty("tempfoldername").equals("")) { // Absolute.
                tempDestination = settings.getStringProperty("tempfoldername").concat(FILE_SEPARATOR);
            } else { // Relative.
                tempDestination = backupPath.concat(FILE_SEPARATOR).concat("temp").concat(FILE_SEPARATOR);
            }
        } else { // No temp folder.
            tempDestination = backupPath.concat(FILE_SEPARATOR);
        }
    }

    // The actual backup should be done here.
    public void doWorlds(String backupName) throws Exception {
        
        // Loops each world that needs to backed up, and do the required copies.
        while (!worldsToBackup.isEmpty()) {
            String currentWorldName = worldsToBackup.removeFirst();

            // Check for split backup.
            if (splitBackup) {

                // Init backup path variable.
                String thisWorldBackupPath;

                // Check if we have a different container for worlds.
                if (!worldContainer.equals(".")) { // Custom.
                    thisWorldBackupPath = backupPath.concat(FILE_SEPARATOR).concat(worldContainer).concat(currentWorldName);
                } else {
                    thisWorldBackupPath = backupPath.concat(FILE_SEPARATOR).concat(currentWorldName);
                }
                
                // Check this worlds folder exists.
                FileUtils.checkFolderAndCreate(new File(thisWorldBackupPath));

                // Set up destinations for temp and full backups.
                String thisWorldBackupFolder;

                // Modify paths if using temp folder.
                if (useTemp) {
                    thisWorldBackupFolder = tempDestination.concat(currentWorldName).concat(FILE_SEPARATOR).concat(backupName);
                } else {
                    thisWorldBackupFolder = thisWorldBackupPath.concat(FILE_SEPARATOR).concat(backupName);
                }

                // Copy the current world into it's backup folder.
                if (settings.getBooleanProperty("worldeditfix")) {
                    thisWorldBackupFolder = thisWorldBackupFolder.concat(FILE_SEPARATOR).concat(currentWorldName);
                }

                // Copy this world.
                FileUtils.copyDirectory(worldContainer.concat(FILE_SEPARATOR).concat(currentWorldName), thisWorldBackupFolder);

                // Check and ZIP folder.
                FileUtils.doCopyAndZIP(thisWorldBackupFolder, thisWorldBackupPath.concat(FILE_SEPARATOR).concat(backupName), shouldZIP, useTemp);

            } else { // Not a split backup.

                // This is the place worlds get copied to before they run through the doCopyOrZIP function.
                thisTempDestination = tempDestination.concat(backupName);
                
                // The folder where we should put the world folders.
                String copyDestination;

                // Check if we have a different container for worlds.
                if (!worldContainer.equals(".")) { // Custom.
                    copyDestination = thisTempDestination.concat(FILE_SEPARATOR).concat(worldContainer).concat(FILE_SEPARATOR).concat(currentWorldName);
                } else {
                    copyDestination  = thisTempDestination.concat(FILE_SEPARATOR).concat(currentWorldName);
                }
                
                // Create this folder.
                FileUtils.checkFolderAndCreate(new File(copyDestination));

                // Copy the current world into it's backup folder.
                FileUtils.copyDirectory(worldContainer.concat(FILE_SEPARATOR).concat(currentWorldName), copyDestination);

            }
        }
    }

    /**
     * Function to get world names to ignore.
     *
     * @return A List[] of the world names we should not be backing up.
     */
    private List<String> getIgnoredWorldNames() {

        // Get skipped worlds form config.
        List<String> worldNames = Arrays.asList(settings.getStringProperty("skipworlds").split(";"));

        // Loop all ignored worlds.
        if (worldNames.size() > 0 && !worldNames.get(0).isEmpty()) {

            // Log what worlds are disabled.
            LogUtils.sendLog(strings.getString("disabledworlds"));
            LogUtils.sendLog(worldNames.toString());
        }

        // Return the world names.
        return worldNames;
    }
}
