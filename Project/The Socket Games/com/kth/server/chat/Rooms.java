package com.kth.server.chat;

import java.util.HashMap;

/**
 * @author Emil Stahl
 */

/***
 * Keeping a list of all available chatrooms and methods for updating the list as well as check for existing chatrooms.
 * 
 */

public class Rooms {

    public HashMap<String, ChatRoom> chatrooms = new HashMap<String, ChatRoom>();

    /**
     * Add a new chatroom to the list of chatrooms
     * 
     * @param name     of the chatroom
     * @param chatRoom the chatroom to be added
     */

    public synchronized void addChatRoom(String name, ChatRoom chatroom) {
        this.chatrooms.put(name, chatroom);
    }

    /**
     * Remove a chatroom from the list of chatrooms
     * 
     * @param name     of the chatroom
     * @param chatRoom the chatroom to be removed
     */

    public synchronized void removeChatRoom(String name, ChatRoom chatroom) {
        this.chatrooms.remove(name);
    }

    /**
     * Check if a chatroom with the given name exists
     * 
     * @param chatRoomName name of the chatroom
     * @return True if the chatroom exists, otherwise  False
     */

    public boolean exist(String chatRoomName) {
        return this.chatrooms.containsKey(chatRoomName);
    }

    /**
     * Iterates the list of chatrooms and adds to a list
     * 
     * @param String with all chatrooms
     */

    public String getChatRooms() {

        String chatRooms = "";
        if (this.chatrooms.size() > 0) {
            for (String key : this.chatrooms.keySet())
                chatRooms += key + "\n";
        } else
            chatRooms = "No chatrooms available";

        return chatRooms;
    }
}