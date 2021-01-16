package bean;
import java.util.Random;

public class GuessBean implements java.io.Serializable {
    
    private int guess;
    private int numberOfGuesses = 0;
    private int secretNumber;
    
    public GuessBean(){
        this.secretNumber = (int)(Math.random()*100);
    }
    
    public void setGuess(int guess){
        this.guess = guess;
        this.numberOfGuesses++;
    }
    
    public int getGuess(){
        return this.guess;
    }
   
    public int getNumberOfGuesses(){
        return this.numberOfGuesses;
    } 
    
    public int getSecretNumber(){
        return this.secretNumber;
    }
      
}
