import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
* @author Emil Stahl
*/

public class connection {

	static String cookie = "";
	static final String url = "http://localhost:8080/index.html", USER_AGENT = "Trash/1.0";
	static int runs, gamesPlayed = 0, totalGuesses = 0, guesses = 0, MAX_RUNS = 100;

	
	static String sendGuess(int guess) throws IOException {
		guesses++;
		totalGuesses++;
		
		String result = "";
		
		HttpURLConnection connection = connect("?guess=" + guess);
		connection.setRequestProperty("Cookie", cookie);
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			if (inputLine.contains("low"))
			result = "LOW";
			else if (inputLine.contains("high")) 
			result = "HIGH";
			else if (inputLine.contains("correct")) 
			result = "CORRECT";
		}
		in.close();
		connection.disconnect();
		return result;
	}
	
	public static void makeGuess(int low, int high) throws IOException, InterruptedException {
		int guess = (int) (low + high) / 2;
		String status = sendGuess(guess);
		if(status == "HIGH") 
		makeGuess(low, guess);
		else if(status == "LOW")
		makeGuess(guess+1, high);
		else if(status == "CORRECT") 
		System.out.println("GAME NR: " + gamesPlayed + "  Finished in: " + guesses + " guesses");
		if(gamesPlayed < runs)
		startNewGame();
	}
	
	static void startNewGame() throws IOException, InterruptedException {
		gamesPlayed++;
		guesses = 0;
		
		HttpURLConnection connection = connect("?newgame=");
		connection.setRequestProperty("Cookie", cookie);
		connection.getResponseCode();
		connection.disconnect();
		System.out.println("STARTING GAME NR " + gamesPlayed);
		makeGuess(0, 100);
	}
	
	static HttpURLConnection connect(String request) throws IOException {
		
		HttpURLConnection connection = (HttpURLConnection) new URL(url + request).openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", USER_AGENT);
		return connection;
	}
	
	static void getCookie() throws Exception {
		
		HttpURLConnection connection = connect("");

		System.out.println(connection.getHeaderFields());
		cookie = connection.getHeaderFields().get("Set-Cookie").get(0).split(";")[0];
		System.out.println("COOKIE: " + cookie);
		connection.disconnect();
	}
	
	public static void main(String[] args) throws Exception {
		
		runs = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_RUNS) ? Integer.parseInt(args[0]) : MAX_RUNS;
		getCookie();
		startNewGame();
		System.out.println("\nThe average number of gueeses for " + runs +  " games was: "  + (double) totalGuesses / gamesPlayed);
	}
}