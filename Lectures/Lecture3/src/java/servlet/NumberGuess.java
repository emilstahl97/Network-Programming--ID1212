package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class NumberGuess extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("start.html");
        HttpSession session = request.getSession(true);
        session.setAttribute("gb", new bean.GuessBean());
        rd.forward(request, response);
        }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher rd;
        String file;
        HttpSession session = request.getSession();
        bean.GuessBean gb = (bean.GuessBean)session.getAttribute("gb");
        gb.setGuess((int)Integer.parseInt((request.getParameter("guess"))));
        if (gb.getSecretNumber() > gb.getGuess())
            file = "guess.jsp?hint=higher";
        else if (gb.getSecretNumber() < gb.getGuess())
            file = "guess.jsp?hint=lower";
        else
            file = "success.jsp";
        rd = request.getRequestDispatcher(file);
        rd.forward(request, response);
    }
}
