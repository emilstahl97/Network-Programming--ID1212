package com.kth.server.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.net.ssl.*;

import com.kth.server.Servable;
import com.kth.server.GameServer;
import com.kth.server.chat.ChatRoom;
import com.kth.server.chat.ChatCommands;

/**
 * @author Emil Stahl
 */

/***
 * This class represents the main chat where users enters the chat. Here they can create new chatrooms or choose to stay
 * in the main chat. 
 */

public class Chat implements Servable {

    private SSLSocket socket;
    private String userName;
    private Rooms rooms;
    private ChatConnections cc;
    private ChatCommands commands;
    public ArrayList<SSLSocket> connections = new ArrayList<SSLSocket>();

    public Chat(SSLSocket s, ChatConnections clConnections, Rooms rooms) {
        this.socket = s;
        this.cc = clConnections;
        this.rooms = rooms;
        this.cc.updateList(s);
        this.commands = new ChatCommands();

    }

    /**
     * Handles the serving of the chat to the user, called from ClientHandler and
     * parses until users types '-q' to quit.
     * 
     * 
     * @param input  to read in commands from the users socket
     * @param output to send back data to the users socket
     */

    public void serve(BufferedReader input, PrintWriter output) {
        
        try {
        
            output.println("What's your name? ");
            this.userName = input.readLine();
            this.cc.addUser(this.userName);
            this.newConnection();
            output.println("\nWelcome to the chat, " + this.userName + ":" + this.commands.getCommandInfoString() + "\n");
            String message = "";

            while ((message = input.readLine()) != null) {
                if (message.equals("-q")) {
                    this.cc.removeUser(this.socket, this.userName);
                    this.broadcastLeave();
                    break;
                }
                if (message.equals("-users")) {
                    output.println("\nThese are the current active users in the main chat: \n" + this.cc.getUsers() + "\n");
                }
                if (message.equals("-rooms")) {
                    output.println("\nThese are the current active chatrooms: \n" + rooms.getChatRooms());
                }
                if (message.equals("-where")) {
                    output.println(this.whereIsUser());
                }
                if (message.equals("-help")) {
                    output.println(this.commands.getCommandInfoString());
                }
                if (message.contains("-file")) {
                    this.handleFileReceiving(message);
                }
                if (message.equals("-chatroom")) {
                    this.handleChatroom(input, output);
                } else {
                    this.broadcast(message);
                }
            }
        } catch (IOException ex) {
            GameServer.LOGGER.warning("An IOException occurred in main chat for user " + this.userName + " on connection "  + this.socket);
            ex.printStackTrace();
        } catch (Exception e) {
            GameServer.LOGGER.warning("An Exception occurred in main chat for user " + this.userName + " on connection "  + this.socket);
            e.printStackTrace();
        }
    }

    /**
     * Handles the receiving of files, it splits the message from the user on space
     * to get the file size and filename that is required for receiving the file.
     * 
     * @param message from the user which contains filesize and filename
     * @throws Exception
     */

    private void handleFileReceiving(String message) throws Exception {
        String[] splitted = message.split(" ");
        String fileSize = splitted[1];
        int fileSizeInt = Integer.parseInt(fileSize);
        String fileName = splitted[2];
        this.broadcast("sending file '" + fileName + "'");
        this.broadcast("-file " + fileSize + " " + fileName);
        this.receiveFile(fileSizeInt, fileName);
    }

    /**
     * Handles the creation or getting of chatrooms depending on if a chatroom with
     * the specified name exists or not. It calls the serve method that handlles the
     * chatroom logic.
     * 
     * @param input  to read from the users socket
     * @param output to write to the users socket
     * @throws Exception
     */

    private void handleChatroom(BufferedReader input, PrintWriter output) throws Exception {
        output.println("Enter the name of the chatroom");
        String name = input.readLine();
        if (rooms.exist(name)) {
            ChatRoom cr = rooms.chatrooms.get(name);
            cr.newConnection(this.userName, this.socket);
            cr.addUser(this.socket, this.userName);
            this.cc.removeUser(this.socket, this.userName);
            cr.serve(this.userName, this.socket, this.cc, this.rooms, input, output);

        } else {
            ChatRoom chatroom = new ChatRoom(name);
            rooms.addChatRoom(name, chatroom);
            chatroom.addUser(this.socket, this.userName);
            this.cc.removeUser(this.socket, this.userName);
            chatroom.serve(this.userName, this.socket, this.cc, this.rooms, input, output);
        }
    }

    /**
     * Sending the specified message to all other current users in the main chat
     * 
     * 
     * @param message to send to all other users
     * @throws Exception
     */

    public void broadcast(String message) throws Exception {

        if (!isCommand(message)) {

            PrintStream out;

            if (this.cc.connections.size() >= 2) {
                for (SSLSocket s : this.cc.connections) {

                    if (s != this.socket) {
                        out = new PrintStream(s.getOutputStream());
                        out.println("\n" + this.userName + ": " + message);
                    }
                }
            }
        }
    }

    /**
     * Informs all other users in the main chat that a user has left the chat
     * 
     * @throws Exception
     */

    public void broadcastLeave() throws Exception {
        PrintStream out;
        for (SSLSocket s : this.cc.connections) {
            out = new PrintStream(s.getOutputStream());
            out.println("\n" + this.userName + " left the chat");
        }
    }

    /**
     * Informs all other users in the main chat that a user has joined the chat
     * 
     * @throws IOException
     */

    private void newConnection() throws IOException {
        for (SSLSocket s : this.cc.connections) {
            try {
                if (s != this.socket) {
                    PrintStream out = new PrintStream(s.getOutputStream());
                    out.println("\n" + this.userName + " joined the chat:");
                }
            } catch (IOException exception) {
            }
        }
    }

    /**
     * Tells the users in which chatroom it is currently in
     * 
     * @return String containing the username and the name of the chatroom
     */

    private String whereIsUser() {
        if (this.cc.users.contains(this.userName))
            return this.userName + " are in Main Chat";
        else {
            for (ChatRoom cr : this.rooms.chatrooms.values()) {
                if (cr.users.contains(this.userName))
                    return this.userName + " are in the chatroom: " + cr.getChatRoomName();
            }
        }
        return "Could not find " + this.userName + " in any chatroom";
    }

    /**
     * Checks if the command from the user is a command or not. Used to prohibit
     * sending commands to other users.
     * 
     * @param message from user
     * @return boolean indicating if the message is a command or not
     */

    private boolean isCommand(String message) {
        return Arrays.stream(this.commands.getCommands()).anyMatch(message::equals);
    }

    /**
     * Broadcasting the specified file to all other users in the chatroom
     * 
     * @param data       containing the data of the file
     * @param size       of the file
     * @param dataLength length of the byte array
     * @throws Exception
     */

    private void sendFile(byte[] data, int size, int dataLength) throws Exception {

        System.out.println("In sendFile");

        if (this.cc.connections.size() >= 2) {
            for (SSLSocket s : this.cc.connections) {

                if (s != this.socket) {
                    OutputStream outputStream = s.getOutputStream();
                    outputStream.write(data, 0, dataLength);
                    outputStream.flush();
                }
            }
        }
    }

    /**
     * Handles the receiving of files when a user sends a file for the server to
     * broadcast
     * 
     * @param fileSize size of the file
     * @param fileName name of the file
     * @throws Exception
     */

    private void receiveFile(int fileSize, String fileName) throws Exception {

        InputStream inputStream = this.socket.getInputStream();
        byte[] data = new byte[fileSize];
        inputStream.read(data, 0, data.length);
        System.out.println("The file \"" + fileName + "\" was uploaded to server successfully!");

        this.sendFile(data, fileSize, data.length);
    }
}