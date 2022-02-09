package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.GPUPTask;
import task.TasksManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "WorkerTaskServlet", urlPatterns = "/worker/task")
public class WorkerTaskServlet extends HttpServlet {
    public Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        resp.setContentType("application/json");

        if(req.getParameter("getTaskToDo") != null)
        {
            GPUPTask gpupTask = tasksManager.pollTaskReadyForWorker();
            String gpupTaskJson = gson.toJson(gpupTask, GPUPTask.class);
            resp.getWriter().write(gpupTaskJson);
        }
    }
}
