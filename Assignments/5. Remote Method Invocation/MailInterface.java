import java.rmi.Remote;

public interface MailInterface extends Remote {
    
    String getMail(String host, String username, String password, int port, String IP) throws Exception;
}