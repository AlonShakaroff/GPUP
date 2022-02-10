package servlets.tasks.worker;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import target.TargetForWorker;
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getHeader("updateStatus") != null) {
            TargetForWorker targetForWorker = gson.fromJson(req.getReader(), TargetForWorker.class);

            tasksManager.updateTargetsStatusAndResult(targetForWorker);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else if(req.getParameter("registerToTask") != null) {
            String taskName = req.getHeader("taskName");
            tasksManager.getTaskForServerSide(taskName).addWorker();
            tasksManager.getTaskDetailsDTO(taskName).addWorker();
        }
        else if(req.getParameter("unregisterFromTask") != null) {
            String taskName = req.getHeader("taskName");
            tasksManager.getTaskForServerSide(taskName).removeWorker();
            tasksManager.getTaskDetailsDTO(taskName).removeWorker();
        }
    }
}
