package com.bukkitbackup.full.webplatform;

import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.utils.LogUtils;

/**
 *
 * @author Domenic Horner
 */
public class RunAction {
    
    private final Settings settings;
    private final Strings strings;

    public RunAction(Settings settings, Strings strings) {
        this.settings = settings;
        this.strings = strings;
    }
    
    
    public String[] handleRequest(String requestParm) {
        
        LogUtils.sendDebug("Handling Action request: "+requestParm);

        String actionResult = null;
        String[] returnError = new String[]{"An error occured handling the action request.", "500 Internal Server Error", "text/plain"};

        if (requestParm.equals("reload")) {
            actionResult = "{'error':'true'}";
        } else if (requestParm.equals("dobackup")) {
            actionResult = "{'error':'true'}";
        }

        if (actionResult != null) {
            LogUtils.sendDebug("Handling Action request success: "+requestParm);
            return new String[]{actionResult, "200 OK", "text/plain"};
        } else {
            LogUtils.sendDebug("Handling Action request failure: "+requestParm);
            return returnError;
        }
    }
    
}
