package task;

import target.Target;

public abstract class Task implements Runnable{
    protected final String taskName;

    public Target getTarget() {
        return target;
    }

    protected final Target target;

    public Task(String taskName, Target target){
        this.taskName = taskName;
        this.target = target;
    }
    public  String getTaskName() {
        return taskName;
    }
}
