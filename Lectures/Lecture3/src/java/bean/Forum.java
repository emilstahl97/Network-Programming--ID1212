package bean;
import java.util.ArrayList;

public class Forum{
    private ArrayList posts;

    public Forum(){
        posts = new ArrayList();
    }
    
    public void addPost(Post p){
        posts.add(p);
    }
    
    public ArrayList getPosts(){
        return posts;
    }
}