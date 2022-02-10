package servlets.tasks;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.TasksManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TasksOperationServlet", urlPatterns = "/tasks/operation")
public class TaskOperationServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getParameter("operation").equalsIgnoreCase("start")) {
            tasksManager.addTaskExecutorThread(req.getHeader("taskName"));
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }
}
