package com.bukkitbackup.full.ftp;

import com.bukkitbackup.full.config.Settings;
import com.bukkitbackup.full.config.Strings;
import com.bukkitbackup.full.utils.FileUtils;
import com.bukkitbackup.full.utils.LogUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bukkit.Server;
import sun.net.ftp.FtpClient;

public class FTPUploader extends FtpClient implements Runnable {

    private Settings settings;
    private Strings strings;

    private static FtpClient ftpClient;
    private String fileToUpload;

    public FTPUploader(Server server, Settings settings, Strings strings, String fileToUpload) {
        this.settings = settings;
        this.strings = strings;
        this.fileToUpload = fileToUpload;
    }

    public void run() {
        FtpToServer(settings.getStringProperty("ftpserveraddress"), settings.getIntProperty("ftpserverport"), settings.getStringProperty("ftpusername"), settings.getStringProperty("ftppassword"), settings.getStringProperty("ftpdirectory"));
    }

    public void FtpToServer(String connAddress, int connPort, String connUser, String connPassword, String connTargetDIR) {

        if (connAddress.equals("") || ((connPort < 0) && (connPort > 65535)) || connUser.equals("")) {
            System.out.println("FTP: Fail - Connection settings incorrect.");
            return;
        }

        if (fileToUpload.isEmpty()) {
            System.out.println("FTP: Fail - No file to upload.");
            return;
        }

        System.out.println(fileToUpload);

        byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
        try {
            System.out.println("FTP: Connecting: " + connAddress+":"+connPort);
            ftpClient = new FtpClient(connAddress, connPort);
            System.out.println("FTP: Connection Estblished: " + connAddress);

            ftpClient.login(connUser, connPassword);
            System.out.println("FTP: User " + connUser + " login OK");
            System.out.println("FTP: Hello Message: " + ftpClient.welcomeMsg);

            System.out.println("FTP: Changing to: " + connTargetDIR);
            ftpClient.cd(connTargetDIR);

            System.out.println("FTP: Attempting to put file...");
            ftpClient.binary();

            System.out.println("Entering FTP");
            FileInputStream in = new FileInputStream(fileToUpload.toString());
            OutputStream out = ftpClient.put(fileToUpload.substring(fileToUpload.lastIndexOf("\\") + 1));
            while (true) {
                int bytes = in.read(buffer);
                if (bytes < 0) {
                    break;
                }
                out.write(buffer, 0, bytes);
            }
            System.out.println("FTP Done");
            out.close();
            in.close();

        } catch (IOException ex) {
            LogUtils.exceptionLog(ex, "Exeption encountered!");
            //Logger.getLogger(FTPUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        if (ftpClient != null) {
            try {
                ftpClient.closeServer();
            } catch (IOException ex) {
            }
            ftpClient = null;
        }
    }
}
