<jsp:useBean class="bean.GuessBean" id="gb" scope="session"></jsp:useBean>
<!DOCTYPE html>

<html>
    <head><title>success.jsp</title></head>
    <body>
        
    You made it in <%=gb.getNumberOfGuesses()%> guess(es). Press button to try again.
    <form action="/Lecture3/NumberGuess" method="GET">
        <input type="submit" value="New game.">
    </form>
   
    </body>
    
</html>
