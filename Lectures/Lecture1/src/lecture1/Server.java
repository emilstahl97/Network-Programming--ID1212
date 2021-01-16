package lecture1;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class Server{
    public static void main(String[] args) throws Exception{
        ServerSocket ss = new ServerSocket(1234);
        Socket s = null;
        String text = "";
        while( (s = ss.accept()) != null){
            BufferedReader indata =
                    new BufferedReader(new InputStreamReader(s.getInputStream()));
            while( (text = indata.readLine()) != null){
                System.out.println("Received:" + text);
            }
        s.shutdownInput();
        }
    }
}
