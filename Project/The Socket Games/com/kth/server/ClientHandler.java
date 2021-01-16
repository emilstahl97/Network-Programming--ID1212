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