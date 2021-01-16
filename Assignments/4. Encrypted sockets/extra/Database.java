import java.util.HashMap;

/**
* @author Emil Stahl
*/

public class Database {

    HashMap<String, Integer> secretNumbers = new HashMap<String, Integer>();
    HashMap<String, Integer> guesses = new HashMap<String, Integer>(); 

    public boolean existingClient(String cookie) {
        return secretNumbers.containsKey(cookie);
    }

    public void addNum(String cookie, Integer num) {
        secretNumbers.put(cookie, num);
        Integer guess = -1;
        guesses.put(cookie, guess);
        System.out.println("Secret number for client " + cookie + " is " +  num);
    }

    public Integer getNum(String cookie) {
        return secretNumbers.get(cookie);
    }

    public void incrementGuess(String cookie) {
        Integer nrguesses = guesses.get(cookie);
        nrguesses++;
        guesses.put(cookie, nrguesses);
    }

    public Integer getGuesses(String cookie) {
        return guesses.get(cookie);
    }
}