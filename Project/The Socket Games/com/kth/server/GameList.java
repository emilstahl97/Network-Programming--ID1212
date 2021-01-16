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