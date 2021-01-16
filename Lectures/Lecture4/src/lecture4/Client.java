package lecture4;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class Client{
    public static void main(String[] args){
        SSLSocketFactory sf = (SSLSocketFactory)SSLSocketFactory.getDefault();
        for(int i = 0; i < sf.getSupportedCipherSuites().length; i++)
            System.out.println("SF " + i + ":" + sf.getSupportedCipherSuites()[i]);
        HttpsURLConnection.setDefaultSSLSocketFactory(sf);
        SSLSocket socket = null;
        String host = "www.lu.se";
        //String host = "localhost";
        try{
            socket = (SSLSocket)sf.createSocket(host,443); //default HTTPS port
        }
        catch(MalformedURLException e){
            System.out.println(e.getMessage());
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
        for(int i = 0; i < socket.getSupportedCipherSuites().length; i++)
            System.out.println("SS " + i + ": " + socket.getSupportedCipherSuites()[i]);
        //String[] cipher = {"SSL_DH_anon_WITH_RC4_128_MD5"};
        String[] cipher = {"TLS_AES_128_GCM_SHA256"};
        //String[] cipher = {"TLS_RSA_WITH_AES_128_CBC_SHA"};
        socket.setEnabledCipherSuites(cipher);
        
        for(int i = 0; i < socket.getEnabledCipherSuites().length; i++)
            System.out.println("SE" + i + ":" + socket.getEnabledCipherSuites()[i]);
        
        PrintWriter writer = null;
        BufferedReader reader = null;
        try{
            socket.startHandshake();
        }
        catch(IOException e){
            System.out.println("*************" + e.getMessage());
        }
        try{
            //writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
        writer.println("GET / HTTP/1.1");
        writer.println("Host: " + host);
        writer.println("");
        writer.flush();
        System.out.println("HTTP-request sent");
        
        try{
            String str;
            while( (str=reader.readLine()) != null)
                System.out.println(str);
            writer.close();
            reader.close();
            socket.close();
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
}
