package servlets;

import users.UserManager;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/chat/logout"})
public class LogoutServlet extends HttpServlet {

<<<<<<< Updated upstream

=======
//
>>>>>>> Stashed changes
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String usernameFromSession = SessionUtils.getUsername(request);
//        UserManager userManager = ServletUtils.getUserManager(getServletContext());
//
//        if (usernameFromSession != null) {
//            System.out.println("Clearing session for " + usernameFromSession);
//            userManager.removeUser(usernameFromSession);
//            SessionUtils.clearSession(request);
//
//            // used mainly for the web version. irrelevant in the desktop client version
//            response.sendRedirect(request.getContextPath() + "/index.html");
//        }
//    }

}