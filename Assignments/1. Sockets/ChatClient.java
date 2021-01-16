import java.io.*;
import java.net.Socket;

/**
* @author Emil Stahl
*/

public class ChatClient {
    public static void main(String[] args) throws Exception {
        
        String username = args[0];
        Socket socket = new Socket("localhost", 1234);

        new Thread(new ChatSend(socket, username)).start();
        receive(socket);        
    }

    static void receive(Socket socket) {
        try {
            BufferedReader indata = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(true)
                System.out.println(indata.readLine());     
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ChatSend implements Runnable {
        Socket socket; 
        String username;

        ChatSend(Socket socket, String username) {
            this.socket = socket;
            this.username = username;
        }

        public void run() {
        
            try {
                PrintStream out = new PrintStream(socket.getOutputStream());
                BufferedReader indata = new BufferedReader(new InputStreamReader(System.in));
                String text;
                out.println(username);
                System.out.println("Enter text to send");
                while( (text = indata.readLine()) != null)
                    out.println(text);

            socket.shutdownOutput();
            } 
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}