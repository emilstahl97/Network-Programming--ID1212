package com.kth.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.PatternSyntaxException;

import javax.net.ssl.SSLSocket;

/**
 * @author Emil Stahl
 */

/**
* This class handles the sending of messages and files for the chat client.
*
*/

public class ChatSend implements Runnable {
    private static final int MAX_FILE_SIZE = 1024 * 1024;
    SSLSocket socket;

    ChatSend(SSLSocket socket) {
        this.socket = socket;
    }

    public void run() {

        try {
            PrintStream out = new PrintStream(socket.getOutputStream());
            BufferedReader indata = new BufferedReader(new InputStreamReader(System.in));
            String text;
            System.out.println("Connected to: " + socket.getInetAddress());
            while ((text = indata.readLine()) != null) {
                if (text.equals("-sendFile")) {
                    System.out.println("Enter the name of the file you want to send");
                    String fileName = indata.readLine();
                    try {
                        handleFileSending(fileName, out, socket);
                    } catch (Exception e) {
                    }
                }
                if (text.equals("-listFiles"))
                    this.listFiles();

                if (text.contains("-cat"))
                    this.printFile(text);

                else
                    out.println(text);
            }

            socket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Splits the message from the user to get filename and printing the file to
     * standard output
     * 
     * @param message from the user
     * @throws IOException
     */

    private void printFile(String message) throws IOException {

        try {

            String[] splitted = message.split(" ");
            String fileName = splitted[1];
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                System.out.println(sb.toString());
            } catch (FileNotFoundException e) {
                System.out.println("The file " + fileName + " does not exist: " + e.getMessage());
            }
        } catch (PatternSyntaxException e) {
            System.out.println(e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Please specify the file");
        }
    }

    /**
     * Printing a list of all files in the directory where received files are stored
     * 
     * @throws IOException
     */

    private void listFiles() throws IOException {

        try {
            String current = new File(".").getCanonicalPath();
            String path = current + "/com/kth/client/Files/";
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            StringBuilder sb = new StringBuilder();
            sb.append("\n");

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    sb.append(listOfFiles[i].getName() + "\n");
                } else if (listOfFiles[i].isDirectory()) {
                    sb.append(listOfFiles[i].getName() + "\n");
                }
            }
            System.out.println(sb.toString());
        } catch (NullPointerException exception) {
            System.out.println("Directory not found: " + exception.getMessage());
        }
    }

    /**
     * Handling sending of files to the server
     * 
     * @param fileName of the file to send
     * @param output   to the serer
     * @param socket   to server
     * @throws Exception
     */

    private void handleFileSending(String fileName, PrintStream output, SSLSocket socket) {
        try {

            File file = getFile(fileName);
            int fileSize = this.getFileSize(file);
            FileInputStream fileInputStream = new FileInputStream(file);
            output.println("-file " + fileSize + " " + fileName);
            this.sendFile(fileInputStream, fileSize, socket);
        } catch (FileNotFoundException filenoException) {
            System.out.println("Could not find a file with the name \"" + fileName + "\"");
        } catch (Exception exception) {
            System.out.println("An exception occurred while sending the file " + fileName + " " + exception.getMessage());
        }
    }

    /**
     * Sending file to server
     * 
     * @param fileInputStream with the file to send
     * @param size            of the file to send
     * @param socket          to the server
     * @throws Exception
     */

    private void sendFile(FileInputStream fileInputStream, int size, SSLSocket socket) throws Exception {

        byte[] data = new byte[size];

        OutputStream outputStream = socket.getOutputStream();
        fileInputStream.read(data);
        outputStream.write(data, 0, data.length);
        outputStream.flush();
        System.out.println("File was successfully sent to server");
    }

    /**
     * Gets the specified file from the filesystem
     * 
     * @param fileName
     * @return File object
     * @throws NullPointerException
     * @throws IOException
     * @throws FileNotFoundException 
     */

    private File getFile(String fileName) throws NullPointerException, FileNotFoundException, IOException {

        String current = new java.io.File(".").getCanonicalPath();
        return new File(current + "/" + fileName);
    }

    /**
     * Get file size of the specified file
     * 
     * @param file
     * @return size of file
     * @throws Exception
     */

    private int getFileSize(File file) throws Exception {

        int size = (int) file.length();

        if (MAX_FILE_SIZE < size)
            throw new Exception();

        return size;
    }
}