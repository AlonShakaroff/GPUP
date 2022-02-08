package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.TasksManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TasksServlet", urlPatterns = "/tasks/operation")
public class TaskOperationServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getHeader("operation").equalsIgnoreCase("start")) {
            Boolean isIncremental;
            if(req.getHeader("isIncremental").equalsIgnoreCase("true"))
                isIncremental = true;
            else
                isIncremental = false;
            tasksManager.addTaskExecutorThread(req.getHeader("taskName"), isIncremental);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }
}
