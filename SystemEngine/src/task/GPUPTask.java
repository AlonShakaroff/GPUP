package task;

import com.google.gson.Gson;
import com.sun.security.ntlm.Client;
import dtos.GPUPTaskDto;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import target.TargetForWorker;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public abstract class GPUPTask implements Runnable{
    protected final String taskName;
    protected final TargetForWorker target;
    protected final String taskType;
    protected String workerName;

    public TargetForWorker getTarget() {
        return target;
    }

    public GPUPTask(String taskName, TargetForWorker target, String taskType){
        this.taskName = taskName;
        this.target = target;
        this.taskType = taskType;
        this.workerName = "";
    }

    public GPUPTask(GPUPTaskDto gpupTaskDto) {
        this.taskName = gpupTaskDto.getTaskName();
        this.target = gpupTaskDto.getTarget();
        this.taskType = gpupTaskDto.getTaskType();
        this.workerName = gpupTaskDto.getWorkerName();
    }
    public String getTaskName() {
        return taskName;
    }

    public String getTaskType() { return taskType; }

    @Override
    public String toString(){return target.getName();}

    private String calcPath(String curGraphPath,String curDate){
        return( curGraphPath + "/" + taskName + " - " + curDate);
    }

    private String getDate(){
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM.yyyy (HH;mm;ss)");
        Date now = new Date();
        return sdfDate.format(now);
    }

    protected void uploadTaskStatusToServer() {
        Gson gson = new Gson();
        String targetForWorkerJson = gson.toJson(this.target,TargetForWorker.class);
        RequestBody body = RequestBody.create(targetForWorkerJson,MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(Constants.WORKER_TASK_PAGE)
                .addHeader("updateStatus","updateStatus")
                .addHeader("workerName",workerName)
                .post(body)
                .build();

        try {
            Response response = HttpClientUtil.runSyncWithRequest(request);
            Objects.requireNonNull(response.body()).close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public String getWorkerName() { return workerName; }

    public void setWorkerName(String workerName) { this.workerName = workerName; }
}
