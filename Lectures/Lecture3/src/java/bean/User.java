package bean;

public class User{
    private String nickname;
    private String email;
    
    public User(){}
    
    public void setNickname(String s){
        this.nickname = s;
    }
    
    public void setEmail(String s){
        this.email = s;
    }
    
    public String getNickname(){
        return nickname;
    }
    
    public String getEmail(){
        return email;
    }
}