package com.kth.client;

import java.io.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author Emil Stahl
 */

 /**
  * Main class for chat client. It creates a socket to the server and starts a new thread for sending messages.
  * This class handles the receiving of files and messages.
  */

public class Client {

    public static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";

    public static void main(String[] args) throws Exception {

       CreateSocketFactory cfs = new CreateSocketFactory();
       SSLSocketFactory socketFactory = cfs.create();
       HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
       
       SSLSocket socket = (SSLSocket) socketFactory.createSocket("localhost", 8080);
       
       new Thread(new ChatSend(socket)).start();
       receive(socket);
    }

    /**
     * Method for receiving messages from the server. If the server sends '-file' it
     * tells the client to prepare for receiving of a file.
     * 
     * @param socket to the server
     */

    static void receive(SSLSocket socket) {
        try {
            BufferedReader indata = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = "";
            while (true) {
                message = indata.readLine();
                if (message.contains("-file")) {
                    String[] splitted = message.split(" ");
                    String fileSize = splitted[2];
                    int fileSizeInt = Integer.parseInt(fileSize);
                    String fileName = splitted[3];
                    receiveFile(socket, fileSizeInt, fileName);
                } else {
                    System.out.println(ANSI_GREEN + message + ANSI_RESET);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handling the receiving of files and stores the file to disk.
     * 
     * @param socket   to the server
     * @param fileSize of the file to be received
     * @param fileName of the file to be received
     * @throws IOException
     */

    static void receiveFile(SSLSocket socket, int fileSize, String fileName) throws IOException {

        InputStream inputStream = socket.getInputStream();
        byte[] data = new byte[fileSize];
        inputStream.read(data, 0, data.length);
        String current = new File(".").getCanonicalPath();
        FileOutputStream fileOutputStream = new FileOutputStream(current + "/com/kth/client/Files/" + fileName);
        fileOutputStream.write(data);
        fileOutputStream.flush();
        fileOutputStream.close();
        System.out.println("The file \"" + fileName + "\" was downloaded successfully!");
    }
}