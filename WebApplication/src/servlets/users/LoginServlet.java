package servlets.users;
import com.google.gson.Gson;
import dtos.WorkerDetailsDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import utils.ServletUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@WebServlet(name = "LoginServlet" , urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    boolean isAdmin;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName = req.getParameter("adminUsername");
        if(userName!=null)
            isAdmin = true;
        else{
            userName = req.getParameter("workerUsername");
            isAdmin = false;
        }
        userName = userName.trim();
        userName = userName.toLowerCase();

        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        resp.setContentType("text/plain");
        if(userManager.isUserExists(userName)){
            resp.getWriter().println("The chosen user name is taken");
            resp.setStatus(400);
        }
        else {
            req.getSession(true).setAttribute("username", userName);
            if(isAdmin)
                userManager.addAdmin(userName);
            else {
                userManager.addWorker(userName);
                Gson gson = new Gson();
                WorkerDetailsDto workerDetailsDto = userManager.getWorkerDetailsDto(userName);
                String workerDetailsDtoJson = gson.toJson(workerDetailsDto,WorkerDetailsDto.class);
                resp.getWriter().println(workerDetailsDtoJson);
            }
            resp.setStatus(200);
        }
    }
}
