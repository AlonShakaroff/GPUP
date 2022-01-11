package task;

import target.Target;

public abstract class GPUPTask implements Runnable{
    protected final String taskName;
    protected final Target target;
    protected ExecutorThread taskManager;

    public Target getTarget() {
        return target;
    }

    public GPUPTask(String taskName, Target target, ExecutorThread taskManager){
        this.taskName = taskName;
        this.target = target;
        this.taskManager = taskManager;
    }
    public  String getTaskName() {
        return taskName;
    }

    @Override
    public String toString(){return target.getName();}
}
