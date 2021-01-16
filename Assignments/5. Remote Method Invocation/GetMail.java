/**
* Class for fetching and parsing e-mails. Called from Server.java.
*
* @author Emil Stahl
* Date: December 9th, 2020
*/

import java.util.Arrays;
import java.util.Properties;
import com.sun.mail.imap.*;
import javax.mail.*;
import javax.mail.search.FlagTerm;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class GetMail {

    public static String getMail(String host, String username, String password, int port) throws Exception{
        
        Session session = Session.getDefaultInstance(new Properties());
        Store store = session.getStore("imaps");
        store.connect(host, port, username, password);
        Folder inbox = store.getFolder( "INBOX" );
        inbox.open( Folder.READ_ONLY );
        
        // Fetch unseen messages from inbox folder
        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), true));

        Message message = messages[messages.length - 1];

        return parseEmail(message);
    }

    private static String parseEmail(Message message) throws Exception {
        
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("From: " + message.getFrom()[0] + "\n");
        sb.append("Subject: " + message.getSubject() + "\n\n");
        sb.append(parseHTML(message.getContent().toString()) + "\n\n");

        return sb.toString();
    }
    
    private static String parseHTML(String html) {
        
        StringBuilder sb = new StringBuilder();
        Document document = Jsoup.parse(html);
        Elements paragraphs = document.getElementsByTag("p");
    
        for (Element paragraph : paragraphs) 
            sb.append(paragraph.text() +  "\n");

        return sb.toString();
    }
}