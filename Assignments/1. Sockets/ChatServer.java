import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.net.InetAddress;
 
/**
* @author Emil Stahl
*/

public class ChatServer {

    public static void main(String[] args) throws Exception{
        
        ServerSocket serversocket = new ServerSocket(1234);
        ArrayList<Socket> connections = new ArrayList<Socket>();

        System.out.println("Server now running on ip: " + InetAddress.getLocalHost() + ":" + serversocket.getLocalPort());
        
        while(true){
            
            try {
                Socket connection = serversocket.accept();
                ClientThread t = new ClientThread(connection);
                connections.add(connection);
                t.updateList(connections);
                new Thread(t).start();   
            } 
            catch (IOException exception) { 
                serversocket.close();
            }
        }
    }
}

class ClientThread implements Runnable {
    
    Socket connection;
    String text = "";
    ArrayList<Socket> connections;
    
    public ClientThread(Socket connection) {
        this.connection = connection;
        this.connections = new ArrayList<Socket>();
    }

    public void updateList(ArrayList<Socket> list) {
        this.connections = list;
    }   

    public void broadcast(String username, String message) {
        for(Socket s: this.connections) {
            try {
                if(s != this.connection) {
                PrintStream out = new PrintStream(s.getOutputStream());
                out.println("\n" + username + ": " + message);
            }
            } 
            catch (IOException exception) {}
        }
    }

    public void newConnection(String username) {
        for(Socket s: this.connections) {
            try {
                if(s != this.connection) {
                PrintStream out = new PrintStream(s.getOutputStream());
                out.println("\n"+username + " joined the chat:");
                }
            } 
            catch (IOException exception) {}
        }
    }

    public void run() {
        
        try {
            BufferedReader indata = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String username = indata.readLine();
            newConnection(username);
            while( (text = indata.readLine()) != null){
            System.out.println(username + ": " + text);
            broadcast(username, text);
            }
        } 
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}       