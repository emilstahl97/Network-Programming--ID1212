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