package lecture2;
import java.io.*;
import java.net.*;

public class HttpClient{
    
    public static void main(String[] args) throws Exception{
	String host = "www.csc.kth.se";
	int port = 80;
	String file = "utbildning/kth/kurser/DD1310/";
	Socket s =
	    new Socket(host,port);
	
	PrintStream utdata =
	    new PrintStream(s.getOutputStream());
	utdata.println("GET /" + file + " HTTP/1.1");
	utdata.println("User-Agent: Mozilla");
        //utdata.println();
	s.shutdownOutput();
	
	BufferedReader indata =
	    new BufferedReader(new InputStreamReader(s.getInputStream()));
	String str = "";
	while( (str = indata.readLine()) != null){
	    System.out.println(str);
	}
	s.close();
    }
}