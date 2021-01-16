package com.kth.server.games;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.kth.server.AbstractGame;
import com.kth.server.Servable;

/***
 * Reference implementation of a Servable class.
 * 
 */

public class TheSecretWord extends AbstractGame implements Servable {

	private static final int GUESS_LIMIT = 5;
	private final String secret = "the secret word";
	private final String prompt = "Guess the secret word or enter 'q' to quit";
	private int numGuesses = 0; // explicit initialization -- would be 0 by default
	private static final String GAME_NAME = "The Secret Word";

	@Override
	public void serve(BufferedReader input, PrintWriter output) throws IOException {
		output.println("Welcome to the Secret Word game.\n" + prompt);
		String userInput = input.readLine().trim();
		while (!userInput.equals("q") && numGuesses < GUESS_LIMIT) {
			numGuesses++;
			if (userInput.equals(secret)) {
				StringBuilder sb = new StringBuilder(getRandomMessage(kudos));
				sb.append("\nEnter your initials to join the high score board!");
				output.println(sb.toString());
				String inits = input.readLine().trim();
				if (getBestScore(GAME_NAME) != null) {
					setBestScore(GAME_NAME, getBestScore(GAME_NAME).getScore() + 1, inits);
				} else
					setBestScore(GAME_NAME, 1, inits);
				return; // end the serve method, thus ending the game
			} else {
				output.println("You said '" + userInput + "' but that is not the secret word.\n" + prompt);
				userInput = input.readLine().trim();
			}
		}
		output.println(getRandomMessage(goodbyes));
	}

	private String getRandomMessage(String[] array) {
		int randomChoice = (int) (Math.random() * array.length);
		String s = array[randomChoice];
		return s;
	}

	private final String[] kudos = { "Yay -- you won!  Way to go, you!!", "Amazing.  You're a clever one, you are.",
			"Did you figure that out by yourself or did you have help?  You WON!",
			"Brilliant -- but shhhhhh.  Don't give away the secret. ;-)" };
	private final String[] goodbyes = { "Oh well, better luck next time.", "Take a break -- come back and try again.",
			"Sorry, but you didn't win.  Have a go again, later.", "Don't over think it.  You can get this.",
			"Hint: Follow my instructions to the letter ;-)" };
}
