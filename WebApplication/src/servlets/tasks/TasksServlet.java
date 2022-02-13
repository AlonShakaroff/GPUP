package servlets.tasks;

import com.google.gson.Gson;
import dtos.TaskDetailsDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import target.FileChecker;
import target.TargetGraph;
import task.TasksManager;
import task.compilation.CompilationTaskInformation;
import task.simulation.SimulationTaskInformation;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TasksServlet", urlPatterns = "/tasks")
public class TasksServlet extends HttpServlet {
    public Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getParameter("selectedTaskName") != null)
        {
            String taskInfoName = req.getParameter("selectedTaskName");
            String infoAsString;

            if(tasksManager.isTaskExists(taskInfoName))
            {
                TaskDetailsDto taskInfo = tasksManager.getTaskDetailsDTO(taskInfoName);
                infoAsString = this.gson.toJson(taskInfo, TaskDetailsDto.class);

                resp.getWriter().write(infoAsString);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //Task not exists in the system
            {
                resp.getWriter().println("The task " + taskInfoName + " doesn't exist in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getParameter("task") != null) //Requesting for task-info
        {
            String taskName = req.getParameter("task");

            if(tasksManager.isTaskExists(taskName)) //The task exists in the system
            {
                String infoAsString = null;

                if(tasksManager.isSimulationTask(taskName)) //Requesting for simulation task
                {
                    SimulationTaskInformation simulationInfo = tasksManager.getSimulationTaskInformation(taskName);
                    infoAsString = this.gson.toJson(simulationInfo, SimulationTaskInformation.class);

                    resp.addHeader("taskType", "simulation");
                }
                else  //Requesting for compilation task
                {
                    CompilationTaskInformation compilationInfo = tasksManager.getCompilationTaskInformation(taskName);
                    infoAsString = this.gson.toJson(compilationInfo, CompilationTaskInformation.class);

                    resp.addHeader("taskType", "compilation");
                }

                resp.getWriter().write(infoAsString);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //Task not exists in the system
            {
                resp.getWriter().println("The task " + taskName + " doesn't exist in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getParameter("getProgress") != null)
        {
            String taskName = req.getParameter("getProgress");
            if(tasksManager.isTaskExists(taskName)) {
                TargetGraph targetGraph = tasksManager.getTaskForServerSide(taskName).getTargetGraph();
                Integer amountOfChosenTargets = targetGraph.howMuchAreChosen();
                Integer amountOfFinishedOrSkipped = targetGraph.howMuchAreFinishedOrSkipped();
                resp.addHeader("amountOfChosenTargets", amountOfChosenTargets.toString());
                resp.addHeader("amountOfFinishedOrSkipped", amountOfFinishedOrSkipped.toString());
                Boolean isFinishedOrStopped = (targetGraph.isTaskFinished() ||
                        tasksManager.getTaskDetailsDTO(taskName).getTaskStatus().equalsIgnoreCase("Stopped"));
                resp.addHeader("isFinished",isFinishedOrStopped.toString());
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else {
                resp.getWriter().println("The task " + taskName + " doesn't exist in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else //Invalid request
        {
            resp.getWriter().println("Invalid parameter!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //----------------------------------------------------doPost----------------------------------------//
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getHeader("simulation") != null) //Uploaded simulation task
        {
            SimulationTaskInformation newTaskInfo = this.gson.fromJson(req.getReader(), SimulationTaskInformation.class);
            if(!tasksManager.isTaskExists(newTaskInfo.getTaskName())) //No task with the same name was found
            {
                tasksManager.addSimulationTask(newTaskInfo);

                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " uploaded successfully!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                FileChecker fileChecker = new FileChecker();
                try {
                    TargetGraph targetGraph = fileChecker.createTargetGraphFromXml(ServletUtils.getGraphsManager(getServletContext()).
                            getGraphFile(newTaskInfo.getGraphName().toLowerCase()).toPath());
                    targetGraph.markTargetsAsChosen(newTaskInfo.getTargetsToExecute());
                    if (newTaskInfo.isIncremental() && req.getHeader("oldTaskName") != null){
                        targetGraph.copyStatusAndResult(tasksManager.getTaskForServerSide(req.getHeader("oldTaskName")).getTargetGraph());
                    }
                    tasksManager.addTaskDetailsDTO(newTaskInfo.getTaskName(), newTaskInfo.getTaskCreator(),
                            TargetGraph.TaskType.SIMULATION, newTaskInfo.getTargetsToExecute(), targetGraph);
                    tasksManager.addTaskForServerSide(newTaskInfo.getTaskName(), TargetGraph.TaskType.SIMULATION,"New" ,targetGraph);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else //A task with the same name already exists in the system
            {
                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " already exists in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getHeader("compilation") != null) //Uploaded compilation task
        {
            CompilationTaskInformation newTaskInfo = this.gson.fromJson(req.getReader(), CompilationTaskInformation.class);
            if(!tasksManager.isTaskExists(newTaskInfo.getTaskName())) //No task with the same name was found
            {
                tasksManager.addCompilationTask(newTaskInfo);

                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " uploaded successfully!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                FileChecker fileChecker = new FileChecker();
                try {
                    TargetGraph targetGraph = fileChecker.createTargetGraphFromXml(ServletUtils.getGraphsManager(getServletContext()).
                            getGraphFile(newTaskInfo.getGraphName().toLowerCase()).toPath());
                    targetGraph.markTargetsAsChosen(newTaskInfo.getTargetsToExecute());
                    if (newTaskInfo.isIncremental() && req.getHeader("oldTaskName") != null){
                        targetGraph.copyStatusAndResult(tasksManager.getTaskForServerSide(req.getHeader("oldTaskName")).getTargetGraph());
                    }
                    tasksManager.addTaskDetailsDTO(newTaskInfo.getTaskName(), newTaskInfo.getTaskCreator(),
                                                TargetGraph.TaskType.COMPILATION, newTaskInfo.getTargetsToExecute(), targetGraph);
                    tasksManager.addTaskForServerSide(newTaskInfo.getTaskName(), TargetGraph.TaskType.COMPILATION,"New" ,targetGraph);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else //A task with the same name already exists in the system
            {
                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " already exists in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if (req.getParameter("selectedTaskName") != null && req.getParameter("status") != null){  //update task status
            String selectedTaskName = req.getParameter("selectedTaskName");
            String status = req.getParameter("status");
            if(tasksManager.isTaskExists(selectedTaskName)) {
                tasksManager.getTaskDetailsDTO(selectedTaskName).setTaskStatus(status);
                if (status.equals("Finished")) {
                    tasksManager.getTaskDetailsDTO(selectedTaskName).setCanRunIncrementally(
                            tasksManager.getTaskForServerSide(selectedTaskName).getCanRunIncrementally());
                }
            }
            else //Task not exists in the system
            {
                resp.getWriter().println("The task " + selectedTaskName + " doesn't exist in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        }
        else //invalid header for uploading new task to system
        {
            resp.addHeader("message", "Error in uploading task to server!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
