package task;

import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import target.Target;

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
}
