import java.io.BufferedReader;
import java.io.InputStream;;
import java.net.ServerSocket;
import java.io.FileReader;
import java.io.File;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.IOException;  
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.UUID;

/**
* @author Emil Stahl
*/

public class HttpServer {

	static int getGuess(String token) {

		int guess;
		try {
			guess = Integer.parseInt(token.split("guess=")[1]);
		} catch (NumberFormatException e) {
			guess = -1;
		}
		return guess;
	}

	public static void main(String[] args) throws IOException {

		System.out.println("Creating Serversocket");
		ServerSocket ss = new ServerSocket(8080);
		Game game = new Game();

		while (true) {

			System.out.println("Waiting for client...");
			Integer guess = null;
			String cookie = "";

			Socket s = ss.accept();
			System.out.println("Client connected");
			BufferedReader request = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String str = request.readLine();
			System.out.println(str);

			StringTokenizer tokens = new StringTokenizer(str, " ?");
			tokens.nextToken(); // The word GET
			String requestedDocument = tokens.nextToken();
			String token = tokens.nextToken();
			System.out.println(token);

			if (token.contains("guess=") && token.split("guess=").length > 1)
				guess = getGuess(token);

			while ((str = request.readLine()) != null && str.length() > 0) {
				if (str.contains("Cookie:") && ((str.split("Cookie: clientId=")[1]) != null)) {
					System.out.println(str.split("Cookie: clientId=")[1]);
					if (str.split("Cookie: clientId=")[1] != null)
						cookie = str.split("Cookie: clientId=")[1];
				}
			}

			if (game.existingClient(cookie)) {
				System.out.println("Existing client");
				if (token.contains("newgame=")) {
					System.out.println("Restarting game");
					game.newGame(cookie);
				}

			} else {
				cookie = UUID.randomUUID().toString().replace("-", "");
				game.newGame(cookie);
			}

			System.out.println("Request done");
			s.shutdownInput();

			// Response begins!!!!

			PrintStream response = new PrintStream(s.getOutputStream());

			response.println("HTTP/1.1 200 OK");
			response.println("Server: Trash 0.1 Beta");

			if (requestedDocument.indexOf(".html") != -1)
				response.println("Content-Type: text/html");

			if (requestedDocument.indexOf(".gif") != -1)
				response.println("Content-Type: image/gif");

			response.println("Set-Cookie: clientId=" + cookie);

			response.println();

			String answer = game.check(guess, cookie);
			System.out.println(answer);

			File f = new File("." + requestedDocument);
			BufferedReader buffReader = new BufferedReader(new FileReader(f.getAbsolutePath()));
			
			String firstHalfHtml = "", secondHalfHtml = "", line = "";

			while (!((line = buffReader.readLine()).contains("/**"))) {
				firstHalfHtml += line + "\n";
			}

			while (!((line = buffReader.readLine()).contains("**/"))) {
				answer += line + "\n";
			}

			while (((line = buffReader.readLine()) != null)) {
				secondHalfHtml += line + "\n";
			}

			response.println(firstHalfHtml + answer + secondHalfHtml);

			s.shutdownOutput();
			s.close();
		}
	}
}