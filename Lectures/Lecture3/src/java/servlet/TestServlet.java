package servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TestServlet extends HttpServlet{

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{
        response.setContentType("text/html");
        PrintWriter out = null;
        try{
            out = response.getWriter();
            //HttpSession session = request.getSession(true);
            //session.setAttribute("gb", "abc");
            //ServletContext application = request.getServletContext();
            //application.setAttribute("questions", "abc");
            response.setBufferSize(16384);
            Cookie c = null;
            int value = 1;
            Cookie[] cookies = request.getCookies();
            //out.flush();
            if(cookies == null){
                c = new Cookie("Clientid","" + value);
                c.setMaxAge(30*60);
                response.addCookie(c);
            } else {
                for(Cookie cc : cookies){
                    out.println(cc.getName()+":"+cc.getValue());
                }
                c = cookies[0];
                c.setValue("" + (Integer.parseInt(c.getValue()) + 1));
                response.addCookie(c);
            }
            out.println("HelloWorld");
            out.flush();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}