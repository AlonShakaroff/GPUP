package task;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import target.Target;
import target.TargetGraph;
import task.copilation.CompilationTask;
import task.simulation.SimulationTask;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class ExecutorThread extends Thread{
    private TargetGraph targetGraph;
    private LinkedList<GPUPTask> tasksList;
    private LinkedList<GPUPTask> readyTasksList;
    private String taskName;

    /*---------------------Simulation parameters-----------------------------*/
    private double warningChance;
    private double successChance;
    private boolean isRandom;
    private int processTimeInMS;
    /*---------------------Compilation parameters----------------------------*/
    private String SourceFolderPath;
    private String DestFolderPath;
    /*-----------------------------------------------------------------------*/
    private Boolean isPaused = false;
    private Boolean isStopped = false;
    public Boolean isPauseDummy = false;
    /*-----------------------------------------------------------------------*/

    public ExecutorThread(TargetGraph targetGraph, String taskName, double warningChance, double successChance,
                          boolean isRandom, int processTimeInMS, boolean isIncremental){
        this.targetGraph = targetGraph;
        this.taskName = taskName;
        this.warningChance = warningChance;
        this.successChance = successChance;
        this.isRandom = isRandom;
        this.processTimeInMS = processTimeInMS;
        this.tasksList = new LinkedList<>();
        this.readyTasksList = new LinkedList<>();
        initTasksList(isIncremental);
    }

    public ExecutorThread(TargetGraph targetGraph, String taskName,String SourceFolderPath, String DestFolderPath,int numOfThreads, boolean isIncremental, TextArea runLogTextArea) {
        this.isStopped = false;
        this.targetGraph = targetGraph;
        this.tasksList = new LinkedList<>();
        this.taskName = taskName;
        this.SourceFolderPath = SourceFolderPath;
        this.DestFolderPath = DestFolderPath;
        initTasksList(isIncremental);
    }
    private void initTasksList(boolean isIncremental){
        tasksList.clear();
        for(Target target : targetGraph.getTargetsToRunOnAndResetExtraData(isIncremental)) {
            if (target.getRunStatus().equals(Target.Status.WAITING)) {
                if(this.taskName.equalsIgnoreCase("simulation"))
                    tasksList.addFirst(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance, Target.extractTargetForWorkerFromTarget(target, taskName)));
                else /*compilation*/
                    tasksList.addFirst(new CompilationTask(taskName, SourceFolderPath, DestFolderPath,Target.extractTargetForWorkerFromTarget(target, taskName)));
            }
            else {
                if(this.taskName.equalsIgnoreCase("simulation"))
                    tasksList.addLast(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance, Target.extractTargetForWorkerFromTarget(target, taskName)));
                else /*compilation*/
                    tasksList.addLast(new CompilationTask(taskName, SourceFolderPath, DestFolderPath, Target.extractTargetForWorkerFromTarget(target, taskName)));
            }
        }
    }

    @Override
    public void run() {
        targetGraph.setTaskStartTime(Instant.now());
        targetGraph.getAllTargets().values().forEach(Target::setStartTimeInCurState);
        while (!tasksList.isEmpty()) {
            if (isStopped) { // break if stopped
                readyTasksList.clear();
                break;
            }

            GPUPTask curTask = tasksList.poll();
            if (curTask.target.getStatus().equals(Target.Status.FROZEN)) { // target is frozen
                tasksList.addLast(curTask);
            } else {    // target is waiting (but maybe needs to be skipped)
                Target curTarget = targetGraph.getTarget(curTask.getTarget().getName());
                curTarget.checkIfNeedsToBeSkipped();
                curTask.getTarget().setResult(curTarget.getRunResult());
                curTask.getTarget().setStatus(curTarget.getRunStatus());
                if (curTask.getTarget().getStatus().equals(Target.Status.SKIPPED)) {
                        curTask.getTarget().setRunLog(curTask.getTarget().getRunLog().concat("Target " + curTask.getTarget().getName() +
                            " is skipped because " + curTarget.getResponsibleTargets().toString() + " failed \n\n"));
                } else {
                     readyTasksList.add(curTask);
                }
            }
            targetGraph.refreshWaiting();
        }
        targetGraph.setTaskEndTime(Instant.now());
        targetGraph.setTotalTaskDuration(Duration.between(targetGraph.getTaskStartTime(), targetGraph.getTaskEndTime()));
    }


    public Boolean getPaused() {
        return isPaused;
    }

    public void setPaused(Boolean paused) {
        synchronized (this.isPauseDummy) {
            isPaused = paused;
            if(!paused)
                this.isPauseDummy.notifyAll();
        }
    }

    public Boolean getStopped() {
        return isStopped;
    }

    public void setStopped(Boolean stopped) {
        isStopped = stopped;
    }

    public Boolean getIsPauseDummy() {
        return isPauseDummy;
    }

    public TargetGraph getTargetGraph() {
        return targetGraph;
    }
}
