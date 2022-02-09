package task;

import target.TargetForWorker;

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
}
