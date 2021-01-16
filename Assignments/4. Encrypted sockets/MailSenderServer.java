
/**
* Simple program for sending e-mails thru SMTP
* UASAGE: java MailSenderServer <RCPT TO:> <message>
*
* @author Emil Stahl
* Date: December 4th, 2020
*/

import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MailSenderServer {

    private PrintStream out;
    private BufferedReader in;
    private static final Integer PORT = 587;
    private static final String HOST = "smtp.kth.se";;
    private static String MY_MAIL = "<emilstah@kth.se>", RCPT = MY_MAIL,
            MESSAGE = "This is a test sent from a manual SMTP client";

    void createConnection() throws IOException {

        Socket socket = new Socket(HOST, PORT);
        this.out = new PrintStream(socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(this.in.readLine());
        send("HELO " + HOST);

        startTLS(socket);
    }

    void startTLS(Socket socket) throws IOException, UnknownHostException {
        send("STARTTLS");
        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket SSLsocket = (SSLSocket) socketFactory.createSocket(socket, socket.getInetAddress().getHostAddress(),
                socket.getPort(), true);
        this.out = new PrintStream(SSLsocket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(SSLsocket.getInputStream()));
        send("HELO " + HOST);

        login();
    }

    void login() throws IOException {
        send("AUTH LOGIN");
        send(Base64.getEncoder().encodeToString(Credentials.USERNAME.getBytes()));
        send(Base64.getEncoder().encodeToString(Credentials.PASSWORD.getBytes()));
    }

    void send(String message) throws IOException {

        this.out.println(message);
        this.out.flush();
        System.out.println(this.in.readLine());
    }

    void sendMail(String message) throws IOException {
        send("MAIL FROM: " + MY_MAIL);
        send("RCPT TO: " + RCPT);
        send("DATA");
        send(message + "\r\n.");
        send("QUIT");
    }

    public static boolean validEmail(String emailStr) {
        final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    static String parseEmail(String email) {
        return "<" + email + ">";
    }

    public static void main(String[] args) {

        RCPT = (args.length > 0) && validEmail(args[0]) ? parseEmail(args[0]) : RCPT;
        MESSAGE = (args.length > 1) ? args[1] : MESSAGE;

        try {
            MailSenderServer client = new MailSenderServer();
            client.createConnection();
            client.sendMail(MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}