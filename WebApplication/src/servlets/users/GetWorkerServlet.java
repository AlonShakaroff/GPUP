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

@WebServlet(name = "GetWorkerServlet", urlPatterns = {"/getWorker"})
public class GetWorkerServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        String workerName = req.getHeader("workerName");
        WorkerDetailsDto workerDetailsDto = userManager.getWorkerDetailsDto(workerName);
        String workerDetailsDtoJson = gson.toJson(workerDetailsDto,WorkerDetailsDto.class);
        resp.getWriter().write(workerDetailsDtoJson);
    }
}