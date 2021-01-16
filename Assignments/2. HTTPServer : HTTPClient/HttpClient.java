import java.io.*;
import java.net.*;

public class HttpClient{
    
    public static void main(String[] args) throws Exception{
	String host = "localhost";
	int port = 8080;
	String file = "index.html";
	Socket socket = new Socket(host,port);
	
	PrintStream out = new PrintStream(socket.getOutputStream());
	out.println("GET /" + file + "?guess=1"+" HTTP/1.1");
	out.println("User-Agent: Mozilla");
	
	socket.shutdownOutput();
	
	BufferedReader indata = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	String str = "";
	while( (str = indata.readLine()) != null){
		System.out.println(str);
	}
	socket.close();
    }
}