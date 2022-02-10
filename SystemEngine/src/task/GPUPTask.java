package task;

import com.google.gson.Gson;
import javafx.application.Platform;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import target.TargetForWorker;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class GPUPTask implements Runnable{
    protected final String taskName;
    protected final TargetForWorker target;
    protected final String taskType;

    public TargetForWorker getTarget() {
        return target;
    }

    public GPUPTask(String taskName, TargetForWorker target, String taskType){
        this.taskName = taskName;
        this.target = target;
        this.taskType = taskType;
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

    protected void uploadTaskResultToServer() {
        Gson gson = new Gson();
        String targetForWorkerJson = gson.toJson(this.target,TargetForWorker.class);
        RequestBody body = RequestBody.create(targetForWorkerJson,MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(Constants.WORKER_TASK_PAGE)
                .addHeader("updateStatus","updateStatus")
                .post(body)
                .build();

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                uploadTaskResultToServer();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
            }
        });
    }
}
