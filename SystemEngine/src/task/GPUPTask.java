package task;

import target.TargetForWorker;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class GPUPTask implements Runnable{
    protected final String taskName;
    protected final TargetForWorker target;

    public TargetForWorker getTarget() {
        return target;
    }

    public GPUPTask(String taskName, TargetForWorker target){
        this.taskName = taskName;
        this.target = target;
    }
    public  String getTaskName() {
        return taskName;
    }

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
