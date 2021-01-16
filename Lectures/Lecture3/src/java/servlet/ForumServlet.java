package servlet;
import java.io.*;
import javax.servlet.ServletContext;
import javax.servlet.http.*;
import java.util.ArrayList;

public class ForumServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,HttpServletResponse response)throws IOException{
        PrintWriter out = response.getWriter();
        //response.setContentType("text/html");
        ServletContext sc = getServletContext();
        if(sc.getAttribute("forum")==null){
            sc.setAttribute("forum", new bean.Forum());
        }
    
        HttpSession session = request.getSession(true);
        if(session.isNew()){
            out.println("<h1>New session!</h1>");
            session.setAttribute("user", new bean.User());
            out.println("<form>");
            out.println("Nickname<input type=\"text\" name=\"nickname\"><br>");
            out.println("Email<input type=\"text\" name=\"email\">");
            out.println("<input type=\"submit\"></form>");
        }
    
        if(request.getParameter("email")!=null){
            out.println("<h1>New user!</h1>");
            bean.User u = (bean.User)session.getAttribute("user");
            u.setNickname(request.getParameter("nickname"));
            u.setEmail(request.getParameter("email"));
            out.println("<form>");
            out.println("Text<input type=\"text\" name=\"text\"><br>");
            out.println("<input type=\"submit\"></form>");
        }
        
        if(request.getParameter("text")!=null){
            out.println("<h1>New post!</h1>");
            bean.User u = (bean.User)session.getAttribute("user");
            bean.Forum f = (bean.Forum)sc.getAttribute("forum");
            bean.Post p = new bean.Post();
            p.setText(request.getParameter("text"));
            p.setNickname(u.getNickname());
            f.addPost(p);
            ArrayList posts = f.getPosts();
            for(int i = 0; i < posts.size(); i++){
                p  = (bean.Post)posts.get(i);
                out.println("<b>" + p.getText() + "</b><br>");
                out.println("<i>" + p.getNickname() + "</i><br>");
            }
            out.println("<form>");
            out.println("Text<input type=\"text\" name=\"text\"><br>");
            out.println("<input type=\"submit\"></form>");
        }
        
        out.close();
    }
}