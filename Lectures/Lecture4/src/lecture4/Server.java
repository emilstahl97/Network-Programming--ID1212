package lecture4;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class Server{
    
    public static void main(String[] args){
        SSLServerSocketFactory ssf; // = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        
        try{
            KeyStore ks = null;
            ks = KeyStore.getInstance("JKS", "SUN");
            //ks = KeyStore.getInstance("jks");
            InputStream is = null;
            //is = new FileInputStream(new File("C:/Program Files/Java/jdk1.8.0_271/jre/lib/security/cacerts"));
            is = new FileInputStream(new File("C:/users/stenework/.keystore"));
            char[] pwd = "rootroot".toCharArray();
            ks.load(is,pwd);
            
            
            SSLContext ctx = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, pwd);
            ctx.init(kmf.getKeyManagers(), null, null);
            //ctx.init(null, null, null);
            ssf = ctx.getServerSocketFactory();
            
            
            System.out.println("Supported:");
            for(int i = 0; i < ssf.getSupportedCipherSuites().length; i++)
                System.out.println(ssf.getSupportedCipherSuites()[i]);
            SSLServerSocket ss = null;
            ss = (SSLServerSocket)ssf.createServerSocket(443);
            //String[] cipher = {"SSL_DH_anon_WITH_RC4_128_MD5"};
            String[] cipher = {"TLS_RSA_WITH_AES_128_CBC_SHA"};
            //ss.setEnabledCipherSuites(cipher);
            System.out.println("Choosen:");
            for(int i = 0; i < ss.getEnabledCipherSuites().length; i++)
                System.out.println(ss.getEnabledCipherSuites()[i]);
            SSLSocket socket = (SSLSocket)ss.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String row = null;
            while( (row=reader.readLine()) != null)
                System.out.println(row);
            reader.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}