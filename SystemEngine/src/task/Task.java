package task;

import target.Target;
import target.TargetGraph;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public abstract class Task {

    private final String taskName;
    public Task(String taskName){
        this.taskName = taskName;
    }
    private String calcPath(String curGraphPath,String curDate){
        return( curGraphPath + "/" + taskName + " - " + curDate);
    }
    private String getDate(){
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM.yyyy (HH;mm;ss)");
        Date now = new Date();
        return sdfDate.format(now);
    }
    public void runTaskOnGraph(TargetGraph targetGraph){
        String dirPath = calcPath(targetGraph.getDirectory(),getDate());
        new File(dirPath).mkdirs();

        targetGraph.setTaskStartTime(Instant.now());
        while (!targetGraph.isTaskFinished()){
            for (Target target: targetGraph.getWaitingSet()){
                target.setStatus(Target.Status.IN_PROCESS);

                communicator.printStartTask(this,target);
                fileSaver.printStartTask(target.getName(), taskName);
                communicator.printTargetExtraData(target);
                fileSaver.printExtraData(target.getExtraData());

                runTaskOnTarget(target);
                target.setStatus(Target.Status.FINISHED);

                communicator.printRunResult(target);
                fileSaver.printTargetRunResult(target);

                updateWaitingAndSkipped(target,targetGraph);
                fileSaver.CloseFile();
            }
            targetGraph.recalculateStatusGraph();
        }
        targetGraph.setTaskEndTime(Instant.now());
        targetGraph.setTotalTaskDuration(Duration.between(targetGraph.getTaskStartTime(),targetGraph.getTaskEndTime()));
        communicator.printGraphStats(targetGraph, dirPath);
        targetGraph.canRunIncrementally();
    }

    public void updateWaitingAndSkipped(Target target, TargetGraph targetGraph) {
        if(target.getRunResult() == Target.Result.FAILURE) {
            target.setAllAboveSkipped();
            communicator.printAllTargetsAboveSkipped(target);
            fileSaver.printAllTargetsAboveSkipped(target);
        }
        else { // success or success with warning
            for (Target curTarget : target.getRequiredForSet()) {
                curTarget.determineIfStatusIsWaiting();
                if(curTarget.getRunStatus() == Target.Status.WAITING) {
                    communicator.printTargetReadyToTask(curTarget);
                    fileSaver.printTargetReadyToTask(curTarget.getName(),taskName);
                }
            }
        }
    }

    public abstract void runTaskOnTarget(Target target);
    public String getTaskName() {
        return taskName;
    }
}
