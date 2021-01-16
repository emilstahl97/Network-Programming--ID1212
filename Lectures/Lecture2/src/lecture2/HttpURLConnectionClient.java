package lecture2;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.io.*;

public class HttpURLConnectionClient{
    
    public static void main(String[] args){
	URL url = null;
	try{
	    url = new URL("http","www.csc.kth.se","/utbildning/kth/kurser/DD1310/");
	}
	catch(MalformedURLException e){
	    System.out.println("#1 " + e.getMessage());
	}
	HttpURLConnection con = null;
	try{
	    con = (HttpURLConnection)url.openConnection();
	}
	catch(IOException e){
	    System.out.println("#2 " + e.getMessage());
	}
	con.setRequestProperty("User-Agent","Mozilla");
	try{
	    con.connect();
	}
	catch(IOException e){
	    System.out.println("#3 " + e.getMessage());
	}
	BufferedReader infile = null;
	try{
	    infile = new BufferedReader(new InputStreamReader(con.getInputStream()));
	}
	catch(IOException e){
	    System.out.println("#4 " + e.getMessage());
	}

	String row = null;
	try{
	    while( (row=infile.readLine()) != null){
		System.out.println(row);
	    }
	}
	catch(IOException e){
	    System.out.println("#5 " + e.getMessage());
	}
	System.out.println(con.getHeaderField("Content-Type"));
    }
}
