package com.bukkitbackup.full.webplatform;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HTTPServer implements Runnable {

    private Socket socket;
    private ServerSocket serverSocket;

    // @TODO Need to initalize variables here.
    public HTTPServer() {
        try {
            serverSocket = new ServerSocket(8765);
        } catch (IOException ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        try {
            
            while (true) {
                this.socket = serverSocket.accept();
                process();
            }
        } catch (Exception ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void process() throws Exception {
        //DataInputStream din = new DataInputStream();
        BufferedReader d = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream ot = socket.getOutputStream();
        PrintWriter pw = new PrintWriter(ot);
            //pw.print
        
        BufferedOutputStream out = new BufferedOutputStream(ot);

        String request = d.readLine().trim();
        System.out.println(request);
        StringTokenizer st = new StringTokenizer(request);

        String header = st.nextToken();

        if (header.equals("GET")) {
            String fileName = st.nextToken();
            FileInputStream fin = null;
            boolean fileExist = true;
            try {
                System.out.println("file: "+fileName);
                fin = new FileInputStream("/web"+fileName);
            } catch (Exception ex) {
                fileExist = false;
            }

            String ServerLine = "Server: Backup Internal HTTP Server\r\n";
            String StatusLine = null;
            String ContentTypeLine = null;
            String ContentLengthLine = null;
            String ContentBody = null;

            if (fileExist) {
                StatusLine = "HTTP/1.0 200 OK\r\n";
                ContentTypeLine = "Content-type: text/html\r\n";
                ContentLengthLine = "Content-Length: " + (new Integer(fin.available()).toString()) + "\r\n";
            } else {
                StatusLine = "HTTP/1.0 200 OK\r\n";
                ContentTypeLine = "Content-type: text/html\r\n";
                ContentBody = "<HTML>"
                        + "<HEAD><TITLE>404 Not Found</TITLE></HEAD>"
                        + "<BODY>404 Not Found"
                        + "</BODY></HTML>";
                ContentLengthLine = (new Integer(ContentBody.length()).toString()) + "\r\n";
            }

            pw.print(StatusLine);
            pw.print(ServerLine);
            pw.print(ContentTypeLine);
            pw.print(ContentLengthLine);
            pw.print("\r\n");
            pw.flush();
            
            if (fileExist) {

                byte[] buffer = new byte[1024];
                int bytes = 0;
                while ((bytes = fin.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes);
                    for (int iCount = 0; iCount < bytes; iCount++) {
                        int temp = buffer[iCount];
                        System.out.print((char) temp);
                    }
                }

                fin.close();
            } else {
                out.write(ContentBody.getBytes());
            }
            out.flush();

            out.close();
            socket.close();


        }
    }
}
