/**
* Simple client for fetching e-mails from an RMI server.
*
* @author Emil Stahl
* Date: December 9th, 2020
*/

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) throws UnknownHostException {

        InetAddress inetAddress = InetAddress.getLocalHost();
        
        final int port = Credentials.PORT;
        final String host = Credentials.HOST;
        final String username = Credentials.USERNAME;
        final String password = Credentials.PASSWORD;
        final String IP = inetAddress.getHostAddress();
        final String serverIP = inetAddress.getHostAddress();

        try {
            Registry registry = LocateRegistry.getRegistry(serverIP);
            MailInterface stub = (MailInterface) registry.lookup("GetMail");
            String mail = stub.getMail(host, username, password, port, IP);
            System.out.println("You got mail: \n" + mail);
        } 
        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}