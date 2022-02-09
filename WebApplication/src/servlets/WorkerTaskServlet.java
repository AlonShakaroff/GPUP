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
import java.util.Set;

@WebServlet(name = "WorkerTaskServlet", urlPatterns = "/worker/task")
public class WorkerTaskServlet extends HttpServlet {
    public Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        resp.setContentType("application/json");

        if(req.getParameter("getTaskToDo") != null)
        {
            Set<String> signedToTasks = gson.fromJson(req.getReader(), Set.class);
            GPUPTask gpupTask = tasksManager.pollTaskReadyForWorker(signedToTasks);
            String gpupTaskJson = gson.toJson(gpupTask, GPUPTask.class);
            resp.getWriter().write(gpupTaskJson);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }
}
