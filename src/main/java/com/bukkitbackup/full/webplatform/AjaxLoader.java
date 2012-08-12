package com.bukkitbackup.full.webplatform;

import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.utils.FileUtils;
import com.bukkitbackup.full.utils.LogUtils;
import java.io.File;

/**
 *
 * @author Domenic Horner
 */
public class AjaxLoader {

    private final Settings settings;
    private final Strings strings;

    public AjaxLoader(Settings settings, Strings strings) {
        this.settings = settings;
        this.strings = strings;
    }

    public String[] handleRequest(String requestParm) {
        
        LogUtils.sendDebug("Handling AJAX request: "+requestParm);

        String returnHTML = null;
        String[] returnError = new String[]{"An error occured handling the ajax request.", "500 Internal Server Error", "text/plain"};

        if (requestParm.equals("main")) {
            returnHTML = sendMainTab();
        } else if (requestParm.equals("backups")) {
            returnHTML = sendBackups();
        } else if (requestParm.equals("settings")) {
            returnHTML = sendSettings();
        } else if (requestParm.equals("controls")) {
            returnHTML = sendControls();
        } else if (requestParm.equals("stats")) {
            returnHTML = sendStats();
        } else if (requestParm.equals("logs")) {
            returnHTML = sendLogs();
        }

        if (returnHTML != null) {
            LogUtils.sendDebug("Handling AJAX request success: "+requestParm);
            return new String[]{returnHTML, "200 OK", "text/html"};
        } else {
            LogUtils.sendDebug("Handling AJAX request failure: "+requestParm);
            return returnError;
        }
    }
    
    
        /**
     * List the backups in the backup folder. We can use the parameter to limit
     * the number of results.
     *
     * @param sender The CommandSender.
     * @param amount The amount of results we want.
     */
    private String sendBackups() {
        
        String returnHTML = "";
        
        // Get the backups path.
        String backupDir = settings.getStringProperty("backuppath", "backups");
        
        // Make a list.
        String[] filesList = new File(backupDir).list();

        // Inform what is happenning.
        returnHTML = returnHTML.concat("<p><b>Listing backup directory: \"" + backupDir + "\".</b></p>");

        // Check if the directory exists.
        if (filesList == null) {

            // Error message.
            returnHTML = returnHTML.concat(strings.getString("errorfolderempty"));
        } else {

            // How many files in array.
            int amountoffiles = filesList.length;

            // Send informal message.
            returnHTML = returnHTML.concat("<p><i>" + amountoffiles + " items found</i></p><p><table cellspacing=\"0\" width=\"100%\">");
            returnHTML = returnHTML.concat("<tr><th>Sequence</th><th>Filename</th><th>Actions</th></tr>");
            // Loop through files, and list them.
            for (int i = 0; i < filesList.length; i++) {

                // Get filename of file.
                String filename = filesList[i];

                // Send messages for each file.
                int number = i + 1;
                String className = "";
                if (number % 2 == 0) {
                    className = " class=\"even\"";
                }
                returnHTML = returnHTML.concat("<tr" + className + "><th>" + number + "</th><th>" + filename + "</th><th>Download / Delete</th></tr>");
            }
        }
        returnHTML = returnHTML.concat("</table></p>");
        return returnHTML;
    }

    private String sendMainTab() {
        String returnHTML = "<p><b>Please use the tabs above to navigate this page.</b></p>"
                + "<p>Backup Version: v2.1-dev (Development Build)<br />"
                + "<i>File Identifier: "+FileUtils.getMD5Checksum("plugins/Backup.jar")+".</i></p>"
                + "<p>By Domenic Horner (gamerx)</p>";
        return returnHTML;
    }

    private String sendSettings() {
        return "<p>Backup Settings :)<br />(This area is incomplete. :/)</p>";
    }

    private String sendControls() {
        return "<p>Backup/Server Control :)<br />(This area is incomplete. :/)</p>";
    }

    private String sendStats() {
        return "<p>Statistics :)<br />(This area is incomplete. :/)</p>";
    }

    private String sendLogs() {
        String returnHTML;
        
        returnHTML = "<p>Bukkit Log <small>(Last 50 Lines)</small><br /><br />";
        
        returnHTML = returnHTML.concat("<textarea rows=\"15\" cols=\"120\" readonly>");
        returnHTML = returnHTML.concat(FileUtils.tail(new File("server.log")));
        returnHTML = returnHTML.concat("</textarea></p>");
        
        returnHTML = returnHTML.concat("<p>Backup Debug Log <small>(Last 100 Lines)</small></p>");

        if (new File("plugins/Backup/debug.log").exists()) {

            returnHTML = returnHTML.concat("<textarea rows=\"15\" cols=\"120\" readonly>");
            returnHTML = returnHTML.concat(FileUtils.tail(new File("plugins/Backup/debug.log")));
            returnHTML = returnHTML.concat("</textarea></p>");
        } else {
            returnHTML = returnHTML.concat("Debug is not currently enabled, did you want to enable it?<br />");
            returnHTML = returnHTML.concat("<button id=\"enabledebug\">"
                    + "Enable Debug"
                    + "</button>");
        }
        return returnHTML;
    }

}
