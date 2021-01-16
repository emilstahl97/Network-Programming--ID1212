package com.kth.server;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.*;
import javax.net.ssl.*;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * @author Emil Stahl
 */

 /**
  * Creates a socketfactory for the server
  */

public class CreateSocketFactory {

    public SSLServerSocketFactory create() throws Exception {

        final char[] passWord = "rootroot".toCharArray();
        InputStream inputStream = new FileInputStream(new File(".keystore"));
        KeyStore keyStore = KeyStore.getInstance("JKS", "SUN");
        keyStore.load(inputStream, passWord);
        SSLContext context = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, passWord);
        context.init(keyManagerFactory.getKeyManagers(), null, null);
        SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
        socketFactory = context.getServerSocketFactory();
        
        return socketFactory;
    }
}