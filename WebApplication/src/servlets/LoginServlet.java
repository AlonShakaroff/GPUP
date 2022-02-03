package servlets;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebServlet(name = "LoginServlet" , urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    Set<String> userNames = new HashSet<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName = req.getParameter("userName");
        resp.setContentType("text/plain");
        if(userNames.contains(userName)){
            resp.getWriter().println("The chosen user name is taken");
            resp.setStatus(400);
        }
        else {
            userNames.add(userName);
            resp.getWriter().println("Logged in successfully");
            resp.setStatus(200);
        }
    }
}
