package task;

import target.Target;

public abstract class GPUPTask implements Runnable{
    protected final String taskName;

    public Target getTarget() {
        return target;
    }

    protected final Target target;

    public GPUPTask(String taskName, Target target){
        this.taskName = taskName;
        this.target = target;
    }
    public  String getTaskName() {
        return taskName;
    }

    @Override
    public String toString(){return target.getName();}
}
