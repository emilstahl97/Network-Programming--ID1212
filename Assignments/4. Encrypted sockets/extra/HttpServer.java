
/**
* Simple HTTP Server for playing Guessing Game thru TLS connection
* UASAGE: java HttpServer
*
* @author Emil Stahl
* Date: December 4th, 2020
*/

import java.net.ServerSocket;
import javax.net.ssl.*;
import java.net.Socket;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.UUID;
import java.util.StringTokenizer;
import java.security.*;
import java.security.cert.CertificateException;

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

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException, UnrecoverableKeyException, CertificateException, NoSuchProviderException {

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
		final int portNumber = 443;

		System.out.println("Creating SSL ServerSocket");
		SSLServerSocket ss = (SSLServerSocket) socketFactory.createServerSocket(portNumber);
		Game game = new Game();

		while (true) {

			System.out.println("Waiting for client...");
			Integer guess = null;
			String cookie = "";

			SSLSocket s = (SSLSocket) ss.accept();
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