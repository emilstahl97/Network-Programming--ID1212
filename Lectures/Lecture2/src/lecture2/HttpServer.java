package lecture2;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class HttpServer{

    public static void main(String[] args) throws IOException{
	System.out.println("Creating Serversocket");
	ServerSocket ss = new ServerSocket(8080);
	while(true){
	    System.out.println("Waiting for client...");
	    Socket s = ss.accept();
	    System.out.println("Client connected");
	    BufferedReader request =
		new BufferedReader(new InputStreamReader(s.getInputStream()));
	    String str = request.readLine();
	    System.out.println(str);
	    StringTokenizer tokens =
		new StringTokenizer(str," ?");
	    tokens.nextToken(); // The word GET
	    String requestedDocument = tokens.nextToken();
	    while( (str = request.readLine()) != null && str.length() > 0){
		System.out.println(str);
	    }
	    System.out.println("Frfrgan klar.");
	    s.shutdownInput();
	    
	    PrintStream response =
		new PrintStream(s.getOutputStream());
	    response.println("HTTP/1.1 200 OK");
	    response.println("Server: Trash 0.1 Beta");
	    if(requestedDocument.indexOf(".html") != -1)
		response.println("Content-Type: text/html");
	    if(requestedDocument.indexOf(".gif") != -1)
		response.println("Content-Type: image/gif");
	    
	    response.println("Set-Cookie: clientId=1; expires=Wednesday,31-Dec-20 21:00:00 GMT");

	    response.println();
            if(!"\favicon.ico".equals(requestedDocument)){
                File f = new File("."+requestedDocument);
                FileInputStream infil = new FileInputStream(f);
                byte[] b = new byte[1024];
                while( infil.available() > 0){
                    response.write(b,0,infil.read(b));
                }
                s.shutdownOutput();
                s.close();
            }
        }
    }
}