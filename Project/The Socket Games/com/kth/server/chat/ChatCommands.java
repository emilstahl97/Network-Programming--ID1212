package com.kth.server.chat;

/**
 * @author Emil Stahl
 */

/***
 * This object contains all the commands available to the user with descriptions.
 */

public class ChatCommands {

    private String commandInfo;
    private String[] commands = { "-q", "-users", "-chatroom", "-rooms", "-where", "-sendFile", "-help", "-listFiles", "-cat" };

    public ChatCommands() {
        this.commandInfo = this.buildCommandString();
    }

    /**
     * Builds a string of all the commands that the user can use
     * 
     * @throws String of commands
     */

    private String buildCommandString() {

        StringBuilder sb = new StringBuilder();
        sb.append("\nThese are the available commands:\n");
        sb.append("\n -q to disconnect from the chat");
        sb.append("\n -users to see the current active users");
        sb.append("\n -chatroom to create a new chatroom");
        sb.append("\n -rooms to see all available chatrooms");
        sb.append("\n -where to see which chatroom you are in");
        sb.append("\n -sendFile to send a file to all other users in the current chatroom");
        sb.append("\n -listFiles to see all files in the Files directory");
        sb.append("\n -cat <filename> to see the content of the specified file");
        sb.append("\n -help to see all commands\n");

        return sb.toString();
    }

    /**
     * Get the string of commands
     *
     * @return String of commands with explanations
     */

    public String getCommandInfoString() {
        return this.commandInfo;
    }

    /**
     * Used to prohibit sending of commands to other users
     * 
     * @return string array with commands
     * 
     */

    public String[] getCommands() {
        return this.commands;
    }
}