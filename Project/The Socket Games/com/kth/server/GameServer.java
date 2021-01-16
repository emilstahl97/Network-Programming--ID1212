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