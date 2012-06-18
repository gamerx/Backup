package com.bukkitbackup.full.config;

import com.bukkitbackup.full.utils.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.bukkit.plugin.PluginDescriptionFile;

public class UpdateChecker implements Runnable {

    private Strings strings;
    private PluginDescriptionFile descriptionFile;
    private String clientID;

    public UpdateChecker(PluginDescriptionFile descriptionFile, Strings strings, String clientID) {
        this.descriptionFile = descriptionFile;
        this.clientID = clientID;
        this.strings = strings;
    }

    public void run() {
        
        // Read the version.
        String webVersion = getVersion();

        if(webVersion == null) {
            LogUtils.sendLog("Failed to retrieve latest version information.");
        } else {
            // Check versions and output log to the user.
            if (!webVersion.equals(descriptionFile.getVersion())) {
                LogUtils.sendLog(strings.getString("pluginoutdate", descriptionFile.getVersion(), webVersion));
            } else {
                LogUtils.sendLog(strings.getString("pluginupdate", descriptionFile.getVersion()));
            }
        }
    }

    public String getVersion() {
        String webVersion;
        try {

            // Configure the URL to pull updated from.
            URL updateURL = new URL("http://checkin.bukkitbackup.com/?ver=" + descriptionFile.getVersion() + "&uuid="+clientID+"&name="+descriptionFile.getName()+"&fromplugin");

            // Read from the URL into a BufferedReader.
            BufferedReader bReader = new BufferedReader(new InputStreamReader(updateURL.openStream()));

            // Read the line from the BufferedReader.
            webVersion = bReader.readLine();

            // Close the BufferedReader.
            bReader.close();

            // Return the version.
            return webVersion;
         } catch (MalformedURLException exeption) {
            return null;
         } catch (IOException exeption) {
            return null;
         }
    }
}
