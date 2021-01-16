package lecture1;

import java.io.*;
import java.net.Socket;

public class Client{
    public static void main(String[] args) throws Exception{
        Socket s = new Socket("localhost",1234);
        PrintStream out = new PrintStream(s.getOutputStream());
        BufferedReader indata = new BufferedReader(new InputStreamReader(System.in));
        String text;
        System.out.print("Enter text to send: ");
        while( (text = indata.readLine()) != null){
            out.println(text);
            System.out.print("Enter text to send: ");
        }
        s.shutdownOutput();
    }
}