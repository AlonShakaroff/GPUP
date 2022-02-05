package task;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import target.Target;
import target.TargetGraph;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class ExecutorThread extends Thread{
    private TargetGraph targetGraph;
    private LinkedList<GPUPTask> tasksList;
    private String taskName;
    public TextArea runLogTextArea;
    /*---------------------Simulation parameters-----------------------------*/
    private double warningChance;
    private double successChance;
    private boolean isRandom;
    private int processTimeInMS;
    private int numOfThreads;
    private ThreadPoolExecutor threadExecutor;
    /*---------------------Compilation parameters----------------------------*/
    private String SourceFolderPath;
    private String DestFolderPath;
    /*-----------------------------------------------------------------------*/
    private Boolean isPaused = false;
    private Boolean isStopped = false;
    public Boolean isPauseDummy = false;
    /*-----------------------------------------------------------------------*/

    public ExecutorThread(TargetGraph targetGraph, String taskName, double warningChance, double successChance,
                          boolean isRandom, int processTimeInMS, int numOfThreads, boolean isIncremental, TextArea runLogTextArea){
        this.targetGraph = targetGraph;
        this.taskName = taskName;
        this.warningChance = warningChance;
        this.successChance = successChance;
        this.isRandom = isRandom;
        this.processTimeInMS = processTimeInMS;
        this.tasksList = new LinkedList<>();
        this.numOfThreads = numOfThreads;
        this.threadExecutor = new ThreadPoolExecutor(numOfThreads,numOfThreads,1000,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());
        this.runLogTextArea = runLogTextArea;
        initTasksList(isIncremental);
    }

    public ExecutorThread(TargetGraph targetGraph, String taskName,String SourceFolderPath, String DestFolderPath,int numOfThreads, boolean isIncremental, TextArea runLogTextArea) {
        this.isStopped = false;
        this.targetGraph = targetGraph;
        this.tasksList = new LinkedList<>();
        this.taskName = taskName;
        this.SourceFolderPath = SourceFolderPath;
        this.DestFolderPath = DestFolderPath;
        this.numOfThreads = numOfThreads;
        this.threadExecutor = new ThreadPoolExecutor(numOfThreads,numOfThreads,1000,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());
        this.runLogTextArea = runLogTextArea;
        initTasksList(isIncremental);
    }
    private void initTasksList(boolean isIncremental){
        tasksList.clear();
        for(Target target : targetGraph.getTargetsToRunOnAndResetExtraData(isIncremental)) {
            if (target.getRunStatus().equals(Target.Status.WAITING)) {
                if(this.taskName.equalsIgnoreCase("simulation"))
                    tasksList.addFirst(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance, target,this, runLogTextArea));
                else /*compilation*/
                    tasksList.addFirst(new CompilationTask(taskName,SourceFolderPath,DestFolderPath,target,this, runLogTextArea));
            }
            else {
                if(this.taskName.equalsIgnoreCase("simulation"))
                    tasksList.addLast(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance, target,this, runLogTextArea));
                else /*compilation*/
                    tasksList.addLast(new CompilationTask(taskName,SourceFolderPath,DestFolderPath,target,this, runLogTextArea));
            }
        }
    }

    @Override
    public void run() {
        targetGraph.setTaskStartTime(Instant.now());
        targetGraph.getAllTargets().values().forEach(Target::setStartTimeInCurState);
        while (!tasksList.isEmpty()) {
            if (isStopped) { // break if stopped
                threadExecutor.shutdownNow();
                Platform.runLater(()->{
                    runLogTextArea.appendText("Run stopped!\n"); });
                return;
            }

            GPUPTask curTask = tasksList.poll();
            if (curTask.target.getRunStatus().equals(Target.Status.FROZEN)) { // target is frozen
                tasksList.addLast(curTask);
            } else {    // target is waiting (but maybe needs to be skipped)
                curTask.getTarget().checkIfNeedsToBeSkipped();
                if (curTask.getTarget().getRunStatus().equals(Target.Status.SKIPPED)) {
                    Platform.runLater(()->{
                        runLogTextArea.appendText("Target " + curTask.getTarget().getName() +
                            " is skipped because " + curTask.getTarget().getResponsibleTargets().toString() + " failed \n\n"); });
                } else {  // target is waiting to run, but maybe can't run due to a serial set
                        threadExecutor.submit(curTask);
                }
            }
            targetGraph.refreshWaiting();
        }
        shutdown();
        targetGraph.setTaskEndTime(Instant.now());
        targetGraph.setTotalTaskDuration(Duration.between(targetGraph.getTaskStartTime(), targetGraph.getTaskEndTime()));
        Platform.runLater(()->{
            runLogTextArea.appendText("Total task runtime: " + TargetGraph.getDurationAsString(targetGraph.getTotalTaskDuration()) + "\n\n"); });
    }

    public void shutdown() {
        Platform.runLater(()->{
            runLogTextArea.appendText("Run shutting down...\n\n"); });
        threadExecutor.shutdown();
        while(!threadExecutor.isTerminated()) {}
        Platform.runLater(()->{
            runLogTextArea.appendText("Run finished.\n\n"); });
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
