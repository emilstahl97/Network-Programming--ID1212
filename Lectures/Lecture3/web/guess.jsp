<jsp:useBean class="bean.GuessBean" id="gb" scope="session"></jsp:useBean>
<!DOCTYPE html>


<html>
    <head><title>guess.jsp</title></head>
    <body>
        
    Nope, guess <%=request.getParameter("hint")%>
    You have made <%=gb.getNumberOfGuesses()%> guess(es)
    
    <form action="/Lecture3/NumberGuess" method="POST">
        What's your guess: <input type="text" name="guess">
    </form>
    
    <!-- Cheat: gb.getSecretNumber() -->
   
    </body>
    
</html>
