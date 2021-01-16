package com.kth.server.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.net.ssl.*;
import java.io.*;

import com.kth.server.GameServer;

/**
 * @author Emil Stahl
 */

/***
 * Representing a chatroom.
 * A chatroom has the same functionality as a chat room except the ability to create new chatrooms.  
 */

public class ChatRoom {

    private String ChatRoomName;
    public ArrayList<SSLSocket> connections = new ArrayList<SSLSocket>();
    public ArrayList<String> users = new ArrayList<String>();
    private HashMap<String, SSLSocket> userSockets = new HashMap<String, SSLSocket>();
    private ChatCommands commands;

    ChatRoom(String ChatRoomName) {
        this.ChatRoomName = ChatRoomName;
        this.commands = new ChatCommands();
    }

    /**
     * Handles the chatroom logic and parses commands from the user until the user
     * quits the chatroom
     * 
     * @param userName of the user
     * @param socket   to the user
     * @param cc       ChatConnections object containing all users connected to the chat
     * @param rooms    Rooms object containing all chatroom and their users
     * @param input    BufferedReader from the user
     * @param output   PrintWriter to the user
     */

    public void serve(String userName, SSLSocket socket, ChatConnections cc, Rooms rooms, BufferedReader input, PrintWriter output) {

        try {
        
            String message = "";
            output.println("\nWelcome to the chatroom " + this.getChatRoomName());

            while ((message = input.readLine()) != null) {
                if (message.equals("-q")) {
                    this.removeUser(socket, userName);
                    this.broadcastLeave(userName);
                    cc.backToMainChat(socket, userName);
                    break;
                }
                if (message.equals("-users")) {
                    output.println("\nThese are the current active users in the chatroom: " + this.getChatRoomName() + "\n" + this.getUsers() + "\n");
                }
                if (message.equals("-where")) {
                    output.println(this.whereIsUser(userName, cc, rooms));
                }
                if (message.equals("-rooms")) {
                    output.println("\nThese are the current active chatrooms: \n" + rooms.getChatRooms());
                }
                if (message.equals("-chatroom")) {
                    output.println("\nIt is not possible to create a new chatroom within a chatroom.\nType '-q' to get back to the Main Chat in order to create a new chatroom.");
                }
                if(message.equals("-help")) {
                    output.println(this.commands.getCommandInfoString());
                }
                if (message.contains("-file")) {
                    this.handleFileSending(message, socket, userName);
                } else if (!isCommand(message)) {
                    this.broadcast(message, socket, userName);
                }
            }
        } catch (NullPointerException exception) {
            GameServer.LOGGER.warning("NullPointerException occurred in chatroom " + this.getChatRoomName() + " " + exception.getMessage());
        }
        catch (Exception ex) {
            GameServer.LOGGER.warning("Exception occurred in chatroom " + this.getChatRoomName() + " " + ex.getMessage());
        }
    }

    /**
     * Checks if the command from the user is a command or not. Used to prohibit
     * sending of commands to other users.
     * 
     * @param message from users
     * @return boolean indicating if the command is a command or not
     */

    private boolean isCommand(String message) {
        return Arrays.stream(this.commands.getCommands()).anyMatch(message::equals);
    }

    /**
     * Tells the users in which chatroom it is currently in
     *
     * @return String containing the username and the name of the chatroom
     */

    private String whereIsUser(String userName, ChatConnections cc, Rooms rooms) {
        if (cc.users.contains(userName))
            return userName + " are in Main Chat";
        else {
            for (ChatRoom cr : rooms.chatrooms.values()) {
                if (cr.users.contains(userName))
                    return userName + " are in the chatroom: " + cr.getChatRoomName();
            }
        }
        return "Could not find " + userName + " in any chatroom";
    }

    /**
     * Adds a new user to the chatroom
     * 
     * @param socket   to the user
     * @param userName name of the user
     */

    public synchronized void addUser(SSLSocket socket, String userName) {
        this.connections.add(socket);
        this.users.add(userName);
        this.userSockets.put(userName, socket);
    }

    /**
     * Removes a user from the chatroom
     * 
     * @param socket   to the user
     * @param userName name of the user
     */

    public synchronized void removeUser(SSLSocket socket, String userName) {
        this.connections.remove(socket);
        this.users.remove(userName);
        this.userSockets.remove(userName);
    }

    /**
     * Gets the name of the chatroom
     * 
     * @return name of the chatroom
     */

    public String getChatRoomName() {
        return this.ChatRoomName;
    }

    /**
     * Sending a message to all other users in the chatroom
     * 
     * @param message  to send
     * @param mySelf   connection to the user sending the message
     * @param userName of the user
     */

    public void broadcast(String message, SSLSocket mySelf, String userName) throws Exception {
        PrintStream out;

        if (this.connections.size() >= 2) {
            for (SSLSocket s : this.connections) {

                if (s != mySelf) {
                    out = new PrintStream(s.getOutputStream());
                    out.println("\n" + userName + ": " + message);
                }
            }
        }
    }

    /**
     * Informs all other users in the chatroom that a user has left the chat
     * 
     * @throws Exception
     */

    private void broadcastLeave(String userName) throws Exception {
        PrintStream out;
        if (this.connections.size() >= 1) {
            for (SSLSocket s : this.connections) {
                out = new PrintStream(s.getOutputStream());
                out.println("\n" + userName + " left the chatroom " + this.ChatRoomName);
            }
        }
    }


    /**
     * Informs all other users in the chatroom that a user has joined the chatroom
     * 
     * @param userName of the user
     * @param mySelf   connection to the user 
     * @throws Exception
     */

    public void newConnection(String userName, SSLSocket mySelf) throws Exception {
        PrintStream out;

        if (this.connections.size() >= 1) {
            for (SSLSocket s : this.connections) {

                if (s != mySelf) {
                    out = new PrintStream(s.getOutputStream());
                    out.println("\n" + userName + ": joined the chatroom " + this.getChatRoomName());
                }
            }
        }
    }

    /**
     * Iterates the list of users and adds them to a string
     * 
     * @return String of all users in the chatroom
     */

    public String getUsers() {

        String userString = "";

        for (String s : this.users)
            userString += s + "\n";

        return userString;
    }

    /**
     * Handles broadcasting of files to other users by receiving a file from the
     * sending user and splitting the message to retrieve filesize and filename. It
     * then broadcasts to all other users except the sending user that a file is to
     * be sent.
     * 
     * @param message  from the user
     * @param socket   to the sending user
     * @param userName name of the user
     */

    public void handleFileSending(String message, SSLSocket socket, String userName) throws Exception {
        String[] splitted = message.split(" ");
        String fileSize = splitted[1];
        int fileSizeInt = Integer.parseInt(fileSize);
        String fileName = splitted[2];
        this.broadcast("sending file '" + fileName + "'", socket, userName);
        this.broadcast("-file " + fileSize + " " + fileName, socket, userName);
        this.receiveFile(fileSizeInt, fileName, socket);
    }

    /**
     * Broadcasting a file to all other users in the chatroom
     * 
     * @param data       containing the data of the file to send
     * @param size       size of file
     * @param dataLength of the byte array
     * @param mySelf     connection to the user
     */

    private void sendFile(byte[] data, int size, int dataLength, SSLSocket mySelf) throws Exception {

        System.out.println("In sendFile");

        if (this.connections.size() >= 2) {
            for (SSLSocket s : this.connections) {

                if (s != mySelf) {
                    OutputStream outputStream = s.getOutputStream();
                    outputStream.write(data, 0, dataLength);
                    outputStream.flush();
                }
            }
        }
    }

    /**
     * Handles the receiving of files from users that is to be broadcasted
     * 
     * @param fileSize size of file to be recevied
     * @param fileName name of the file
     * @param socket   connection to the user sending the file
     */

    private void receiveFile(int fileSize, String fileName, SSLSocket socket) throws Exception {

        InputStream inputStream = socket.getInputStream();
        byte[] data = new byte[fileSize];
        inputStream.read(data, 0, data.length);
        System.out.println("The file \"" + fileName + "\" was uploaded to server successfully!");

        this.sendFile(data, fileSize, data.length, socket);
    }
}