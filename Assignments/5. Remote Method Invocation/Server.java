/**
* Simple implementation of a RMI server that handles requests from clients and fetches
* e-mails from an external server thru IMAP.
*
* @author Emil Stahl
* Date: December 9th, 2020
*/

import java.net.InetAddress;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements MailInterface {
        
  public String getMail(String host, String username, String password, int port, String IP) throws Exception {

    System.out.println("Handling request from: " + IP);
    return GetMail.getMail(host, username, password, port);
  }
      
  public static void main(String args[]) {
    
    try {
      
      Server server = new Server();
      InetAddress inetAddress = InetAddress.getLocalHost();
      MailInterface stub = (MailInterface) UnicastRemoteObject.exportObject(server, 0);

      // Bind the remote object's stub in the registry
      Registry registry = LocateRegistry.getRegistry();
      registry.unbind("GetMail");
      registry.bind("GetMail", stub);

      System.err.println("Server started at: " + inetAddress.getHostAddress());
    } 
    catch (Exception e) {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }
}
