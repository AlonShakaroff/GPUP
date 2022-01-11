package task;

import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import target.Target;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class GPUPTask implements Runnable{
    protected final String taskName;
    protected final Target target;
    protected ExecutorThread taskManager;
    protected TextArea runLogTextArea;

    public Target getTarget() {
        return target;
    }

    public GPUPTask(String taskName, Target target, ExecutorThread taskManager, TextArea runLogTextArea){
        this.taskName = taskName;
        this.target = target;
        this.taskManager = taskManager;
        this.runLogTextArea = runLogTextArea;
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
