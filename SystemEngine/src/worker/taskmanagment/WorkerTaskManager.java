package worker.taskmanagment;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dtos.CompilationTaskDto;
import dtos.GPUPTaskDto;
import dtos.SimulationTaskDto;
import javafx.application.Platform;
import main.WorkerMainController;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import task.GPUPTask;
import task.compilation.CompilationTask;
import task.simulation.SimulationTask;
import util.http.HttpClientUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerTaskManager extends Thread {
    private String workerName;
    private ThreadPoolExecutor threadPool;
    private Integer numberOfAllocatedThreads;
    private Integer amountOfTasksRegisteredTo = 0;
    private Set<String> tasksRegisteredToSet;

    public WorkerTaskManager(int threadAmount, String workerName) {
        threadPool = new ThreadPoolExecutor(threadAmount,threadAmount, 1000000, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        this.numberOfAllocatedThreads = threadAmount;
        this.tasksRegisteredToSet = new HashSet<>();
        this.workerName = workerName;
    }

    public Boolean isThreadPoolFull() {
        return threadPool.getActiveCount() == numberOfAllocatedThreads;
    }

    public void addRegisteredTask(String taskName) {
        tasksRegisteredToSet.add(taskName);
        amountOfTasksRegisteredTo++;
    }

    public void removeFinishedTask(String taskName) {
        tasksRegisteredToSet.remove(taskName);
        amountOfTasksRegisteredTo--;
    }

    public void setTasksRegisteredToSet(Set<String> tasksRegisteredToSet) {
        this.tasksRegisteredToSet = tasksRegisteredToSet;
        this.amountOfTasksRegisteredTo = tasksRegisteredToSet.size();
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(500);
            }
            catch (Exception ignore) {}
            if(!isThreadPoolFull() && amountOfTasksRegisteredTo > 0) {
                getGPUPTaskToRunFromServer();
            }

        }
    }

    private void getGPUPTaskToRunFromServer() {
        Gson gson = new Gson();
        String registeredTasksSetJson = gson.toJson(tasksRegisteredToSet, Set.class);

        RequestBody body = RequestBody.create(registeredTasksSetJson, MediaType.parse("application/json"));

        String finalUrl = HttpUrl
                .parse(Constants.WORKER_TASK_PAGE)
                .newBuilder()
                .addQueryParameter("getTaskToDo", "getTaskToDo")
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "POST", body, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    Gson gson = new Gson();
                    String gpupTaskDtoJson = response.body().string();
                    try {
                        if(Objects.requireNonNull(response.header("taskType")).equalsIgnoreCase("simulation"))
                        {
                            SimulationTaskDto simulationTaskDto = gson.fromJson(gpupTaskDtoJson,SimulationTaskDto.class);
                            if(simulationTaskDto != null) {
                                simulationTaskDto.setWorkerName(workerName);
                                threadPool.execute(new SimulationTask(simulationTaskDto));
                            }
                        }
                        else if(Objects.requireNonNull(response.header("taskType")).equalsIgnoreCase("compilation"))
                        {
                            CompilationTaskDto compilationTaskDto = gson.fromJson(gpupTaskDtoJson,CompilationTaskDto.class);
                            if(compilationTaskDto != null) {
                                compilationTaskDto.setWorkerName(workerName);
                                threadPool.execute(new CompilationTask(compilationTaskDto));
                            }
                        }
                    }
                    catch (Exception e) {
                        System.out.println("json error: " + e.getMessage());
                    }
                }
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }
}
