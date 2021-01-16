package com.kth.server;

import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.net.ssl.*;
import javax.net.ssl.SSLServerSocketFactory;

import com.kth.server.chat.Rooms;
import com.kth.server.chat.ChatConnections;

/**
 * @author Emil Stahl
 */

 /**
  * Main class for project. 
  * Provides listening for incoming connections and starts a new thread that handles the client.
  */

public class GameServer {

	public final static Logger LOGGER; 
	private final static int DEFAULT_PORT_NUM = 8080;
	private final static int DEFAULT_MAX_USERS = 10;

	static {
		LOGGER = Logger.getLogger("ServerLog");
	}

	public static void main(String[] args) throws Exception {

		int PORT = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT_NUM;
		int MAX_CONNECTIONS = (args.length > 1) ? Integer.parseInt(args[1]) : DEFAULT_MAX_USERS;

		String refuseNewConnectionMessage = "The server limit of " + MAX_CONNECTIONS
				+ ((MAX_CONNECTIONS == 1) ? " connection" : " connections")
				+ " has been reached.  Please try again, later.";

		Rooms rooms = new Rooms();
		ChatConnections cc = new ChatConnections();
		CreateSocketFactory cfs = new CreateSocketFactory();
		SSLServerSocketFactory socketFactory = cfs.create();

		try (SSLServerSocket socketRequestListener = (SSLServerSocket) socketFactory.createServerSocket(PORT)) {
			LOGGER.info("SERVER: GameServer started on port: " + socketRequestListener.getLocalPort()
					+ "\nMaximum number of users: " + MAX_CONNECTIONS);

			GameTracker.initialize();

			while (true) {

				System.out.println("Waiting for client...");
				SSLSocket socket = (SSLSocket) socketRequestListener.accept();

				int numActiveSockets = Thread.activeCount() - 1;
				if (numActiveSockets < MAX_CONNECTIONS) {
					new Thread(new ClientHandler(socket, cc, rooms)).start();
					numActiveSockets++;
					LOGGER.info("WELCOME: " + socket.getInetAddress() + " Number of current connections: "
							+ numActiveSockets);
				} else {
					PrintWriter out = new PrintWriter(socket.getOutputStream());
					out.println(refuseNewConnectionMessage);
					out.close();
					socket.close();
					LOGGER.warning("SORRY: " + socket.getInetAddress() + ".  Number of current connections: "
							+ numActiveSockets);
				}

			}
		} catch (Exception e) {
			LOGGER.warning("An exception occurred while handling incoming connection");
			e.printStackTrace();
		}
	}
}
package com.kth.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import javax.net.ssl.*;

import com.kth.server.games.*;
import com.kth.server.chat.*;

/**
 * @author Emil Stahl
 */

/***
 * Manages the connection with a remote machine for the lifetime of the thread.
 * Sends menu options and reads the user selection. 
 * 
 * Extends PrintWriter and BufferedReader classes to echoed versions that
 * read/write a copy of the input/output streams to the logger, as well.
 * 
 */

public class ClientHandler implements Runnable {

	private Rooms rooms;
	private SSLSocket socket;
	private ChatConnections cc;
	private final long SCROLL_DELAY = 500L; // half second delay

	public ClientHandler(SSLSocket socket, ChatConnections cc, Rooms rooms) {
		this.socket = socket;
		this.cc = cc;
		this.rooms = rooms;
	}

	@Override
	public void run() {
		try (PrintWriter out = new EchoWriter(socket.getOutputStream(), true);
				BufferedReader br = new EchoReader(new InputStreamReader(socket.getInputStream()))) {
			out.println(); // provide some white space before menu
			while (true) {
				Thread.sleep(SCROLL_DELAY);
				out.println(GameTracker.getGameMenu());
				String choice = br.readLine().trim().toLowerCase();
				if ("q".equals(choice))
					break;
				Object o = new Exception();
				switch (choice) {
				case "0":
					o = new Hangman();
					break;
				case "1":
					o = new DiceRoll();
					break;
				case "2":
					o = new OneInAMillion();
					break;
				case "3":
					o = new Nicomachus();
					break;
				case "4":
					o = new TheSecretWord();
					break;
				case "5":
					o = new HiLo();
					break;
				case "6":
					o = new EvenWins();
					break;
				case "7":
					o = new HiddenWord();
					break;
				case "8":
					o = new RockandPaper();
					break;
				case "9":
					o = new Chat(this.socket, this.cc, this.rooms);
					break;
				default:
					o = new Exception("Game nr: " + choice + " not found.");
				}
				if (o instanceof Servable) {
					GameServer.LOGGER.info("GAME_ON: " + socket.getInetAddress() + " " + o.getClass().getSimpleName());
					out.println(); // whitespace
					((Servable) o).serve(br, out);
					Thread.sleep(SCROLL_DELAY);
					GameServer.LOGGER.info("GAME_OVER: " + socket.getInetAddress() + " " + o.getClass().getSimpleName());
					out.println("\nSession completed: Press Enter/Return to continue.");
					br.readLine(); // provide opportunity for user to see message
				} else if (o instanceof Exception) {
					Exception e = (Exception) o;
					out.println(e.getMessage());
					out.println("Press Enter/Return to continue.");
					br.readLine(); // user is ready to continue
				}
			}
			GameServer.LOGGER.info("GOODBYE: " + socket.getInetAddress() + " Connection closed normally.");

		} catch (NullPointerException e1) {
			GameServer.LOGGER.warning("UH-OH: " + socket.getInetAddress() + " Connection closed abruptly.");
		}
		catch (IOException e2) {
			GameServer.LOGGER.warning("UH-OH: " + socket.getInetAddress() + " Connection closed abruptly.");
		}
		 catch (InterruptedException e3) {
			GameServer.LOGGER.warning("Thread interrupted while sleeping. " + e3.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				GameServer.LOGGER.info("GameThread attempting to close a closed socket");
			}
		}
	}

	/***
	 * Extends PrintWriter to capture and log outgoing data.
	 * 
	 */
	class EchoWriter extends PrintWriter {

		public EchoWriter(OutputStream out, boolean autoFlush) {
			super(out, autoFlush);
		}

		@Override
		public void println(String s) {
			super.println(s);
			if (!s.trim().equals("")) {
				String logString = s;
				logString = logString.replace("\n", " ");
				logString = logString.replace("\t", " ");
				String prefix = "--> " + socket.getInetAddress() + " ";
				GameServer.LOGGER.info(prefix + logString);
			}
		}

	}

	/***
	 * Extends BufferedReader to capture and log incoming data.
	 * 
	 */
	class EchoReader extends BufferedReader {

		public EchoReader(Reader in) {
			super(in);
		}

		@Override
		public String readLine() {
			try {
				String s = super.readLine();
				if (!s.trim().equals("")) {
					String logString = s;
					logString = logString.replace("\n", " ");
					logString = logString.replace("\t", " ");
					String prefix = "<-- " + socket.getInetAddress() + " ";
					GameServer.LOGGER.info(prefix + logString);
				}
				return s;
			} catch (IOException e) {
				return "";
			}
		}
	}
}
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
package com.kth.client;

import java.io.File;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author Emil Stahl
 */

/**
* Creates a socketfactory for the client
*/

public class CreateSocketFactory {

    public SSLSocketFactory create() throws Exception {

        File cert = new File("./server.cer");
        Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new FileInputStream(cert));
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("server", certificate);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        return socketFactory;
    }
}
package com.kth.server;

/**
 * @author Emil Stahl
 */

/***
 * Base class for games. Provides methods for saving/retrieving high scores
 */

public abstract class AbstractGame {

	/**
	 * Retrieves the recorded score object associated with this class, if one is
	 * available.
	 * 
	 * @return the current record, if present; null, otherwise
	 */

	public final BestScore getBestScore(String game) {
		return GameTracker.getBestScore(game);
	}

	/**
	 * Registers a game record with the GameTracker.
	 * 
	 * @param value    an integer score
	 * @param initials initials of the new record holder (three characters)
	 */
	
	public final void setBestScore(String game, int value, String initials) {
		if (initials == null || initials.equals(""))
			initials = "---";
		else if (initials.length() > 3)
			initials = initials.substring(0, 3).toUpperCase();
		else
			initials = initials.toUpperCase();
		GameTracker.setBestScore(game, new BestScore(value, initials));
	}
}

package com.kth.server;

/**
 * @author Emil Stahl
 */

/***
 * Plain old Java object that associates a numeric score (integer) with the
 * player who set the score.
 * 
 */

public class BestScore {
	
	private int score;
	private String holder;

	public BestScore(int score, String holder) {
		this.score = score;
		this.holder = holder;
	}

	public int getScore() {
		return score;
	}

	public String getHolder() {
		return holder;
	}
}
package com.kth.server;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.*;
import javax.net.ssl.*;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * @author Emil Stahl
 */

 /**
  * Creates a socketfactory for the server
  */

public class CreateSocketFactory {

    public SSLServerSocketFactory create() throws Exception {

        final char[] passWord = "rootroot".toCharArray();
        InputStream inputStream = new FileInputStream(new File(".keystore"));
        KeyStore keyStore = KeyStore.getInstance("JKS", "SUN");
        keyStore.load(inputStream, passWord);
        SSLContext context = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, passWord);
        context.init(keyManagerFactory.getKeyManagers(), null, null);
        SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
        socketFactory = context.getServerSocketFactory();
        
        return socketFactory;
    }
}
package com.kth.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * Library that provides methods for retrieving words from a word source. The
 * default source is Google's 10000 most frequently used words in USA English
 * (w/o swear words) and is taken from
 * https://github.com/first20hours/google-10000-english
 *
 */
public final class Dictionary {

	private static Map<Integer, List<String>> mapBySize;
	private static Map<Character, List<String>> mapByFirstCharacter;
	private static List<String> immutableList;
	private static final String DEFAULT_DICT = "google-10000-english-usa-no-swears.txt";

	static {
		List<String> words = new ArrayList<String>();
		words.add("no dictionary loaded");
		try {
			String filename = DEFAULT_DICT;
			words = Files.readAllLines(Paths.get(filename));
		} catch (IOException e) {
			// the phrase 'no dictionary loaded' will populate the dictionary
		}
		immutableList = Collections.unmodifiableList(words);
		mapBySize = words.stream().collect(Collectors.groupingBy(s -> s.length()));
		mapByFirstCharacter = words.stream().collect(Collectors.groupingBy(s -> s.charAt(0)));
	}

	/**
	 * Returns an immutable version of the word source. Immutability is needed so
	 * that a single copy of the dictionary may be stored in memory and used as a
	 * common resource by all threads. While every thread can therefore read freely
	 * from the list, no thread may write to, delete from, or otherwise modify the
	 * list.
	 * 
	 * @return a reference to an immutable word list
	 */
	
	public static List<String> getImmutableList() {
		return immutableList;
	}

	/**
	 * Return a random word beginning with the specified letter
	 * 
	 * @param c the initial character
	 * @return a random word starting with c
	 */
	public static String randomByFirstCharacter(Character c) {
		List<String> words = mapByFirstCharacter.get(c);
		if (words == null)
			return null;
		int size = words.size();
		int randomIndex = (int) (Math.random() * size);
		return words.get(randomIndex);
	}

	/**
	 * Returns a random word beginning with the specified letter
	 * 
	 * @param s the initial letter
	 * @return a random word beginning with s
	 */
	public static String randomByFirstLetter(String s) {
		return randomByFirstCharacter(s.charAt(0));
	}

	/**
	 * Return a random word of the specified size.
	 * 
	 * @param i the size
	 * @return a random word of size i
	 */
	public static String randomBySize(int i) {
		List<String> words = mapBySize.get(i);
		if (words == null)
			return null;
		int size = words.size();
		int randomIndex = (int) (Math.random() * size);
		return words.get(randomIndex);
	}

	/**
	 * Return a random word between min and max characters long, inclusive.
	 * 
	 * @param min the minimum length of a qualifying word
	 * @param max the maximum length of a qualifying word
	 * @return a word meeting the length criteria
	 */
	public static String randomBySize(int min, int max) {
		List<String> words = new ArrayList<String>();
		for (int i = min; i <= max; i++) {
			words.addAll(mapBySize.get(i));
		}
		int randomIndex = (int) (Math.random() * words.size());
		return words.get(randomIndex);
	}

	/**
	 * Returns a random word from the word source
	 * 
	 * @return a random word from the entire dictionary
	 */
	public static String random() {
		int randomIndex = (int) (Math.random() * immutableList.size());
		return immutableList.get(randomIndex);
	}
}

package com.kth.server;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Emil Stahl
 */

 /**
  * Manages a list of all games with short description. 
  * Used for printing menu and other things in GameTracker.
  *
  */

public class GameList {

    public HashMap<String, String> gameDescriptions = new HashMap<String, String>();
    public ArrayList<String> gameNames = new ArrayList<String>();

    public GameList() {

        gameDescriptions.put("Hangman", "Guess the letters to reveal the word.");
        gameDescriptions.put("Dice Roll", "Roll a biggie");
        gameDescriptions.put("One in a Million", "How many guesses will it take?");
        gameDescriptions.put("Nicomachus - Mystic Math", "Pick a secret number and I will divine your selection!");
        gameDescriptions.put("The Secret Word", "Guess the secret word and win!");
        gameDescriptions.put("HiLo", "How HiLo can you go?");
        gameDescriptions.put("Even Wins", "Even wins.");
        gameDescriptions.put("Hidden Word", "Mastermind meets Hangman.");
        gameDescriptions.put("Rock Paper Scissors", "A classic made digital");
        gameDescriptions.put("Chat", "Chat with other players");

        gameNames.add("Hangman");
        gameNames.add("Dice Roll");
        gameNames.add("One in a Million");
        gameNames.add("Nicomachus - Mystic Math");
        gameNames.add("The Secret Word");
        gameNames.add("HiLo");
        gameNames.add("Even Wins");
        gameNames.add("Hidden Word");
        gameNames.add("Rock Paper Scissors");
        gameNames.add("Chat");
    }
}
package com.kth.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Emil Stahl
 */

/***
 * Handles high scores for games and building the menu.
 * 
 */

public class GameTracker {

	private static String gameMenu;
	private static GameList gameList = new GameList();
	private static Map<String, BestScore> bestScores = new HashMap<String, BestScore>();;

	private static final String HIGH_SCORE_FILE = "best-scores.csv";

	/**
	 * Initializes the game database. Should be called on the class when starting
	 * the application. The GameServer class does this when starting the service.
	 * 
	 */
	public static void initialize() {

		loadBestScores();
		gameMenu = buildGameListMenu();
	}

	/**
	 * Prepares a game menu string displaying the game index, game name, current
	 * high score, and high score holder initials. 
	 * 
	 * @return a menu string displaying information on available games and chat
	 */

	public static String buildGameListMenu() {

		String s = String.format("%-8s%-50s%-13s%-3s%n", "=======", "GAME", "BEST SCORE", "INITIALS");
		int b = 0;
		for (int i = 0; i < gameList.gameNames.size(); i++) {
			s += String.format("%7d%s%-50s", b++, " ", gameList.gameNames.get(i));
			if (bestScores.containsKey(gameList.gameNames.get(i))) {
				BestScore r = bestScores.get(gameList.gameNames.get(i));
				s += String.format("%10d%s%3s", r.getScore(), "    ", r.getHolder());
			}
			s += "\n";
			s += String.format("%12s", " ");
			String description = gameList.gameDescriptions.get(gameList.gameNames.get(i).toString()).toString();
			int maxIndex = Math.min(description.length(), 68);
			s += description.substring(0, maxIndex) + "\n";
		}
		s += "\nEnter the number of the game to play or 'q' to exit.";
		return s;
	}

	/**
	 * @return a menu string
	 */

	public static Object getGameMenu() {
		return gameMenu;
	}

	/**
	 * For the indicated game, get the current best score.
	 * 
	 * @param game the game to retrieve best score of
	 * @return the current best score; null, if not found
	 */

	public static BestScore getBestScore(String game) {
		if (bestScores.containsKey(game)) {
			return bestScores.get(game);
		}
		return null;
	}

	/**
	 * Associates a BestScore with the given game. Refreshes the game menu
	 * so that it will display the modified best score.
	 * 
	 * @param game      the game for which a new high score has been achieved
	 * @param bestScore a bestscore object indicating the best score and initials of who set it
	 */

	public static void setBestScore(String game, BestScore bestScore) {
		bestScores.put(game, bestScore);
		gameMenu = buildGameListMenu();
		writeBestScores();
	}

	/**
	 * Write out the best scores to a csv file which can be loaded later
	 */

	private static void writeBestScores() {
		Set<String> scoreInfo = new HashSet<String>();
		Path file = Paths.get(HIGH_SCORE_FILE);
		for (String c : bestScores.keySet()) {
			BestScore r = bestScores.get(c);
			int i = r.getScore();
			String initials = r.getHolder();
			String info = c + "," + i + "," + initials;
			scoreInfo.add(info);
		}
		try {
			Files.write(file, scoreInfo, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve and load the best scores from a csv file. If the file is not
	 * present, then the best score table is effectively reset.
	 */

	private static void loadBestScores() {
		List<String> words = new ArrayList<String>();
		try {
			String filename = HIGH_SCORE_FILE;
			words = Files.readAllLines(Paths.get(filename));
			for (String s : words) {
				String[] data = s.trim().split(",");
				if (data.length == 3) {
					String game = data[0];
					try {
						String c = data[0];
						int i = Integer.parseInt(data[1]);
						String initials = data[2];
						bestScores.put(c, new BestScore(i, initials));
					} catch (NumberFormatException e) {
						// current best value not an integer
						GameServer.LOGGER.warning("While retrieving best scores, could not parse score for " + game);
					}
				}
			}
		} catch (NoSuchFileException e) {
			GameServer.LOGGER.warning("Best scores file not found.  Will create a fresh one.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
package com.kth.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/***
 * Interface for games that read and write across streams.
 *
 */

public interface Servable {

	void serve(BufferedReader input, PrintWriter output) throws IOException;
}
