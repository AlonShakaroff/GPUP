package servlets.tasks.worker;

import com.google.gson.Gson;
import dtos.WorkerDetailsDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import target.Target;
import target.TargetForWorker;
import task.GPUPTask;
import task.TasksManager;
import users.UserManager;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Set;

@WebServlet(name = "WorkerTaskServlet", urlPatterns = "/worker/task")
public class WorkerTaskServlet extends HttpServlet {
    public Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");

        if(req.getParameter("getTaskToDo") != null)
        {
            Set<String> signedToTasks = gson.fromJson(req.getReader(), Set.class);
            GPUPTask gpupTask = tasksManager.pollTaskReadyForWorker(signedToTasks);
            String gpupTaskJson = gson.toJson(gpupTask, GPUPTask.class);
            out.write(gpupTaskJson);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }

        if(req.getHeader("updateStatus") != null) {
            TargetForWorker targetForWorker = gson.fromJson(req.getReader(), TargetForWorker.class);

            tasksManager.updateTargetsStatusAndResult(targetForWorker);
            if(targetForWorker.getStatus() != Target.Status.SKIPPED)
                userManager.getWorkerDetailsDto(req.getHeader("workerName").toLowerCase()).addCredits(targetForWorker.getPricing());
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else if(req.getParameter("registerToTask") != null) {
            WorkerDetailsDto workerDetailsDto = userManager.getWorkerDetailsDto(req.getParameter("workerName").toLowerCase());
            String taskName = req.getParameter("taskName");
            if(workerDetailsDto.getRegisteredTasks().contains(taskName))
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.addHeader("message","Already registered to task " + taskName);
            }
            else {
                resp.setStatus(HttpServletResponse.SC_OK);
                tasksManager.getTaskForServerSide(taskName).addWorker();
                tasksManager.getTaskDetailsDTO(taskName).addWorker();
                workerDetailsDto.registerToTask(taskName.toLowerCase());
            }
        }
        else if(req.getParameter("unregisterFromTask") != null) {
            String taskName = req.getParameter("taskName");
            tasksManager.getTaskForServerSide(taskName).removeWorker();
            tasksManager.getTaskDetailsDTO(taskName).removeWorker();
            userManager.getWorkerDetailsDto(req.getParameter("workerName").toLowerCase()).unregisterFromTask(taskName.toLowerCase());
        }
    }
}
