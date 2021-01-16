import java.util.Random;

/**
* @author Emil Stahl
*/

public class Game {

    static Random rand = new Random();
    static Database database = new Database();

    public void newGame(String cookie) {
        database.addNum(cookie, rand.nextInt(100));
    }

    public boolean existingClient(String cookie) {
        return database.existingClient(cookie);
    }

    public String check(Integer guess, String cookie) {
        String answer;
        database.incrementGuess(cookie);

        if (guess == null)
            return answer = "<p>" + "Welcome" + "</p>";
        if (guess > 100 || guess < 0)
            return answer = "<p>" + "Only guess numbers between 0 and 100" + "</p>";
        if (guess < database.getNum(cookie))
            return answer = "<p>" + guess + " Too low!" + "</p>";
        if (guess > database.getNum(cookie))
            return answer = "<p>" + guess + " Too high!" + "</p>";
        if (guess == database.getNum(cookie))
            return answer = "<p>" + guess + " is correct! You made it in " + database.getGuesses(cookie) + " guesses " + "</p>";
        return "";
    }
}
