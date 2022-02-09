package task;

import dtos.TaskDetailsDto;
import target.TargetGraph;
import task.copilation.CompilationParameters;
import task.copilation.CompilationTask;
import task.copilation.CompilationTaskInformation;
import task.simulation.SimulationParameters;
import task.simulation.SimulationTask;
import task.simulation.SimulationTaskInformation;

import java.util.*;
import java.util.stream.Collectors;

public class TasksManager {

    private static final Map<String, SimulationTaskInformation> simulationTasksMap = new HashMap<>();
    private static final Map<String, CompilationTaskInformation> compilationTasksMap = new HashMap<>();
    private static final Map<String, Set<String>> usersTasks = new HashMap<>();
    private static final Set<String> listOfAllTasks = new HashSet<>();
    private static final Map<String, TaskDetailsDto> taskDetailsDTOMap = new HashMap<>();
    private static final Map<String, TaskForServerSide> taskForServerSideMap = new HashMap<>();
    private static final Map<String, ExecutorThread> taskExecutorThreadMap = new HashMap<>();
    private static final LinkedList<GPUPTask> tasksThatAreReadyForWorkersList = new LinkedList<>();

    public synchronized boolean isTaskExists(String taskName) {
        return simulationTasksMap.containsKey(taskName.toLowerCase()) || compilationTasksMap.containsKey(taskName.toLowerCase());
    }

    public synchronized boolean isSimulationTask(String taskName) { return simulationTasksMap.containsKey(taskName.toLowerCase()); }
    public synchronized boolean isCompilationTask(String taskName) { return compilationTasksMap.containsKey(taskName.toLowerCase()); }

    public synchronized CompilationTaskInformation getCompilationTaskInformation(String taskName) {
        return compilationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized SimulationTaskInformation getSimulationTaskInformation(String taskName) {
        return simulationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized void addSimulationTask(SimulationTaskInformation newTask) {
        simulationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());
    }

    public synchronized void addCompilationTask(CompilationTaskInformation newTask) {
        compilationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());
    }

    public synchronized void addUserTask(String taskCreator, String taskName) {
        if(!usersTasks.containsKey(taskCreator))
            usersTasks.put(taskCreator, new HashSet<>());

        usersTasks.get(taskCreator).add(taskName);
    }

    public synchronized Set<String> getAllTaskList()
    {
        return listOfAllTasks;
    }

    public synchronized Set<String> getUserTaskList(String userName)
    {
        return usersTasks.get(userName.toLowerCase());
    }

    public synchronized void addTaskDetailsDTO(String taskName, String creatorName,
                                               TargetGraph.TaskType taskType,Set<String> targetsToExecute, TargetGraph targetGraph)
    {
        taskDetailsDTOMap.put(taskName.toLowerCase(), new TaskDetailsDto(taskName, creatorName, taskType, targetsToExecute, targetGraph));
    }

    public synchronized TaskDetailsDto getTaskDetailsDTO(String taskName)
    {
        return taskDetailsDTOMap.get(taskName.toLowerCase());
    }

    public synchronized void addTaskForServerSide(String taskName, TargetGraph.TaskType taskType, String taskStatus, TargetGraph targetGraph){
        taskForServerSideMap.put(taskName.toLowerCase(),(new TaskForServerSide(taskName, taskType, taskStatus, targetGraph)));
    }

    public synchronized  TaskForServerSide getTaskForServerSide(String taskName) {
        return taskForServerSideMap.get(taskName.toLowerCase());
    }

    public synchronized void addTaskExecutorThread(String taskName) {
        TargetGraph targetGraph = getTaskForServerSide(taskName).getTargetGraph();
        if(isSimulationTask(taskName)) {
            SimulationParameters parameters = getSimulationTaskInformation(taskName).getSimulationParameters();
            taskExecutorThreadMap.put(taskName.toLowerCase(),new ExecutorThread(targetGraph, "simulation",
                                         parameters.getSuccessWithWarnings(), parameters.getSuccessRate(),
<<<<<<< Updated upstream
                                                parameters.isRandom(),parameters.getProcessingTime(), isIncremental,this));
        }
        else if(isCompilationTask(taskName)) {
            CompilationParameters parameters = getCompilationTaskInformation(taskName).getCompilationParameters();
            taskExecutorThreadMap.put(taskName.toLowerCase(),new ExecutorThread(targetGraph,"compilation",
                                parameters.getSourcePath(),parameters.getDestinationPath(), isIncremental,this));
=======
                                                parameters.isRandom(),parameters.getProcessingTime(),
                                                    getSimulationTaskInformation(taskName).isIncremental()));
        }
        else if(isCompilationTask(taskName)) {
            CompilationParameters parameters = getCompilationTaskInformation(taskName).getCompilationParameters();
            taskExecutorThreadMap.put(taskName.toLowerCase(),new ExecutorThread(targetGraph,taskName,
                                parameters.getSourcePath(),parameters.getDestinationPath(),
                                        getSimulationTaskInformation(taskName).isIncremental()));
>>>>>>> Stashed changes
        }
    }

    public synchronized void addTaskReadyForWorker(GPUPTask task) {
        tasksThatAreReadyForWorkersList.addLast(task);
    }

    public synchronized GPUPTask pollTaskReadyForWorker(Set<String> tasksThatWorkerIsSignedTo) {
        List<GPUPTask> filteredList = tasksThatAreReadyForWorkersList.stream().filter(gpupTask -> tasksThatWorkerIsSignedTo.contains(gpupTask.taskName)).collect(Collectors.toList());
        if(!filteredList.isEmpty()) {
            tasksThatWorkerIsSignedTo.remove(filteredList.get(0));
            return filteredList.get(0);
        }
        else //filtered list is empty
            return null;
    }
}
