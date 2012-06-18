package com.bukkitbackup.full.ftp;

import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.utils.FileUtils;
import com.bukkitbackup.full.utils.LogUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import sun.net.ftp.FtpClient;

public class FTPUploader extends FtpClient implements Runnable {

    private Settings settings;
    private Strings strings;
    private String fileToUpload;
    private static FtpClient ftpClient;

    public FTPUploader(Settings settings, Strings strings, String fileToUpload) {
        this.settings = settings;
        this.strings = strings;
        this.fileToUpload = fileToUpload;
    }

    public void run() {

        // Settings.
        String connAddress = settings.getStringProperty("ftpserveraddress");
        int connPort = settings.getIntProperty("ftpserverport");
        String connUser = settings.getStringProperty("ftpusername");
        String connPassword = settings.getStringProperty("ftppassword");
        String connTargetDIR = settings.getStringProperty("ftpdirectory");


        // Perform checking of settings.
        if (connAddress.equals("") || ((connPort < 0) && (connPort > 65535)) || connUser.equals("")) {
            System.out.println(strings.getString("ftpfailsettings"));
            return;
        }

        // Check the file.
        if (fileToUpload.isEmpty()) {
            System.out.println(strings.getString("ftpfailnofile"));
            return;
        }

        // Initalize the buffer for the upload.
        byte[] buffer = new byte[FileUtils.BUFFER_SIZE];

        try {

            // Start the connection.
            System.out.println(strings.getString("ftpconnecting", connAddress + ":" + connPort));
            ftpClient = new FtpClient(connAddress, connPort);
            System.out.println(strings.getString("ftpestablished", connAddress));

            // Atempt authentication.
            ftpClient.login(connUser, connPassword);
            System.out.println(strings.getString("ftphellomsg", ftpClient.welcomeMsg));

            // Switch to binary mode.
            ftpClient.binary();

            // Change directory if required.
            if(!connTargetDIR.equals("")) {
                ftpClient.cd(connTargetDIR);
                System.out.println(strings.getString("ftpchangedinto", connTargetDIR));
            }

            // Attempt the file upload.
            System.out.println(strings.getString("ftpuploading"));
            FileInputStream in = new FileInputStream(fileToUpload.toString());
            OutputStream out = ftpClient.put(fileToUpload.substring(fileToUpload.lastIndexOf("\\") + 1));
            while (true) {
                int bytes = in.read(buffer);
                if (bytes < 0) {
                    break;
                }
                out.write(buffer, 0, bytes);
            }

            // Notify complete, and close streams.
            System.out.println(strings.getString("ftpuploadcomplete"));
            out.close();
            in.close();

        } catch (Exception ex) {
            LogUtils.exceptionLog(ex);
        }

        if (ftpClient != null) {
            try {
                // Close the ftp Client.
                ftpClient.closeServer();
            } catch (IOException ex) {
                LogUtils.exceptionLog(ex);
            }
            ftpClient = null;
        }
    }
}
