package com.bukkitbackup.full.threading.tasks;

import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.utils.FileUtils;
import static com.bukkitbackup.full.utils.FileUtils.FILE_SEPARATOR;
import com.bukkitbackup.full.utils.LogUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Domenic Horner
 */
public class BackupEverything {

    private final Settings settings;
    private final Strings strings;
    private final String backupPath;
    private final boolean shouldZIP;
    private final boolean useTemp;
    private final String tempDestination;

    public BackupEverything(Settings settings, Strings strings) {

        this.settings = settings;
        this.strings = strings;

        // Get the backup destination.
        backupPath = settings.getStringProperty("backuppath");

        // Get backup properties.
        shouldZIP = settings.getBooleanProperty("zipbackup");
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

    // The actual backup should be done here, as it is run in another thread.
    public void doEverything(String backupName) {

        String thisTempDestination = tempDestination.concat(backupName);
        
        //FileUtils.checkFolderAndCreate(new File(thisTempDestination));

        // Filefiler for excludes.
        FileFilter ff = new FileFilter() {

            /**
             * Files to accept/deny.
             */
            @Override
            public boolean accept(File f) {

                // Disallow server.log and the backuppath.
                if (f.getName().equals(settings.getStringProperty("backuppath"))) {
                    return false;
                }

                if (f.getName().equals("server.log")) {
                    return false;
                }

                return true;
            }
        };

        // Setup Source and destination DIR's.
        File srcDIR = new File(".".concat(FILE_SEPARATOR));
        File destDIR = new File(tempDestination);

        // Copy this world into the doBackup directory, in a folder called the worlds name.
        try {

            // Copy the directory.
            FileUtils.copyDirectory(srcDIR, destDIR, ff, true);

            // Perform the zipping action.
            FileUtils.doCopyAndZIP(tempDestination, backupPath.concat(FILE_SEPARATOR).concat(backupName), shouldZIP, useTemp);

        } catch (FileNotFoundException fnfe) {
            LogUtils.exceptionLog(fnfe, "Failed to copy server: File not found.");
        } catch (IOException ioe) {
            LogUtils.exceptionLog(ioe, "Failed to copy server: IO Exception.");
        }
    }
}
