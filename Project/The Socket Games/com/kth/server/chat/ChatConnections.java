package com.kth.server.chat;

import java.util.ArrayList;
import javax.net.ssl.SSLSocket;

/**
 * @author Emil Stahl
 */

/***
 * Keeping a list of all connections and users in the main chat.
 * 
 */

public class ChatConnections {

    public ArrayList<String> users = new ArrayList<String>();
    public ArrayList<SSLSocket> connections = new ArrayList<SSLSocket>();

    /**
     * Updating the list of all connections to the chat
     * 
     * @param connection to the user
     */

    public synchronized void updateList(SSLSocket connection) {
        this.connections.add(connection);
    }

    /**
     * Updating the list of all users in the chat
     * 
     * @param userName of the user to add
     * 
     */

    public synchronized void addUser(String userName) {
        this.users.add(userName);
    }

    /**
     * Removes a user from the list of connections and the list of users
     * 
     * @param connection to the user
     * @param userName   of the user to remove
     */

    public synchronized void removeUser(SSLSocket connection, String userName) {
        this.connections.remove(connection);
        this.users.remove(userName);
    }

    /**
     * Adds a user to the main chat after it exits from a chatroom
     * 
     * @param connection to the user
     * @param userName   of the user
     */

    public synchronized void backToMainChat(SSLSocket connection, String userName) {
        this.connections.add(connection);
        this.users.add(userName);
    }

    /**
     * Iterates the list of users and adds them to a string
     * 
     * @return userString of all users in the chat
     */

    public String getUsers() {

        String userString = "";

        for (String s : this.users)
            userString += s + "\n";

        return userString;
    }
}