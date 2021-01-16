package bean;

public class Post{
    private String text;
    private String nickname;
    
    public Post(){}
    
    public void setText(String s){
        this.text = s;
    }
    
    public void setNickname(String s){
        this.nickname = s;
    }
    
    public String getText(){
        return text;
    }
    
    public String getNickname(){
        return nickname;
    }
}