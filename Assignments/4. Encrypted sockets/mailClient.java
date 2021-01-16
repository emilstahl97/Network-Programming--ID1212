
/**
* Simple program for fetching e-mails thru IMAP
* UASAGE: java MailClient
*
* @author Emil Stahl
* Date: December 4th, 2020
*/

import javax.net.ssl.SSLSocket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class MailClient {

    private PrintStream out;
    private BufferedReader in;
    private static final Integer PORT = 993;
    private static final String HOST = "webmail.kth.se";
    private static final String LOGIN_TAG = "a001", SELECT_TAG = "a002", GET_TAG = "a003";

    SSLSocket createSocket() throws IOException {

        return (SSLSocket) SSLSocketFactory.getDefault().createSocket(HOST, PORT);
    }

    void createConnection() throws IOException {

        SSLSocket sslsocket = createSocket();
        this.out = new PrintStream(sslsocket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
        login();
    }

    void getBody() throws IOException {

        // SELECT inbox
        this.out.println(SELECT_TAG + " select inbox\r\n");
        this.out.flush();
        getResponse(SELECT_TAG);

        // GET body of mail
        this.out.println(GET_TAG + " fetch 2 body[text]\r\n");
        this.out.flush();
        getResponse(GET_TAG);
    }

    void login() throws IOException {

        this.out.println(LOGIN_TAG + " LOGIN " + Credentials.USERNAME + " " + Credentials.PASSWORD + "\r\n");
        this.out.flush();
        getResponse(LOGIN_TAG);
        getBody();
    }

    void getResponse(String tag) throws IOException {

        String line;

        do {
            line = this.in.readLine();
            System.out.println(line);

        } while (!(line.split(" ")[0].equals(tag)));
    }

    public static void main(String[] args) {

        try {
            MailClient mc = new MailClient();
            mc.createConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}