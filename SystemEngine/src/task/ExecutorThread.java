package task;

import javafx.scene.shape.Path;
import target.Target;
import target.TargetGraph;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class ExecutorThread extends Thread{
    private TargetGraph targetGraph;
    private LinkedList<GPUPTask> tasksList;
    private String taskName;
    /*---------------------Simulation parameters-----------------------------*/
    private double warningChance;
    private double successChance;
    private boolean isRandom;
    private int processTimeInMS;
    private ExecutorService threadExecutor;
    /*---------------------Compilation parameters----------------------------*/
    private String SourceFolderPath;
    private String DestFolderPath;
    /*-----------------------------------------------------------------------*/
    private Boolean isPaused;
    private Boolean isPausedRightNow;
    private Boolean isStopped;
    /*-----------------------------------------------------------------------*/

    public ExecutorThread(TargetGraph targetGraph, String taskName, double warningChance, double successChance,
                          boolean isRandom, int processTimeInMS, int numOfThreads, boolean isIncremental){
        this.isStopped = false;
        this.targetGraph = targetGraph;
        this.taskName = taskName;
        this.warningChance = warningChance;
        this.successChance = successChance;
        this.isRandom = isRandom;
        this.processTimeInMS = processTimeInMS;
        this.tasksList = new LinkedList<>();
        this.threadExecutor = Executors.newFixedThreadPool(numOfThreads);
        initTasksList(isIncremental);
    }

    public ExecutorThread(TargetGraph targetGraph, String taskName,String SourceFolderPath, String DestFolderPath,int numOfThreads, boolean isIncremental) {
        this.isStopped = false;
        this.targetGraph = targetGraph;
        this.taskName = taskName;
        this.SourceFolderPath = SourceFolderPath;
        this.DestFolderPath = DestFolderPath;
        this.threadExecutor = Executors.newFixedThreadPool(numOfThreads);
        initTasksList(isIncremental);
    }
    private void initTasksList(boolean isIncremental){
        for(Target target : targetGraph.getTargetsToRunOnAndResetExtraData(isIncremental)) {
            if (target.getRunStatus().equals(Target.Status.WAITING)) {
                if(this.taskName.equalsIgnoreCase("simulation"))
                    tasksList.addFirst(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance, target));
                else /*compilation*/
                    tasksList.addFirst(new CompilationTask(taskName,SourceFolderPath,DestFolderPath,target));
            }
            else {
                if(this.taskName.equalsIgnoreCase("simulation"))
                    tasksList.addLast(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance, target));
                else /*compilation*/
                    tasksList.addLast(new CompilationTask(taskName,SourceFolderPath,DestFolderPath,target));
            }
        }
    }

    @Override
    public void run(){

       while (!tasksList.isEmpty()){
           if(getStopped())
               shutdown();
           GPUPTask curTask = tasksList.poll();
           if (curTask.target.getRunStatus().equals(Target.Status.FROZEN)) { // target is frozen
               tasksList.addLast(curTask);
           }
           else{    // target is waiting (but maybe needs to be skipped)
               curTask.getTarget().checkIfNeedsToBeSkipped();
               if (curTask.getTarget().getRunStatus().equals(Target.Status.SKIPPED)){
                   System.out.println("Target " + curTask.getTarget().getName() +
                           " is skipped because " + curTask.getTarget().getResponsibleTargets().toString() + "failed \n");
               }
               else{  // target is waiting to run, but maybe can't run due to a serial set
                   if (targetGraph.DoesHaveSerialMemberInProgress(curTask.target))
                       tasksList.addLast(curTask);
                   else {   // target is waiting to run!!!
                       System.out.println("Adding task on target " + curTask.getTarget().getName() + " to thread pool.");
                       curTask.getTarget().setStatus(Target.Status.IN_PROCESS);
                       threadExecutor.execute(curTask);
                   }
               }
           }
           targetGraph.refreshWaiting();
       }
       shutdown();
    }

    public void shutdown() {
        threadExecutor.shutdown();
        while(!threadExecutor.isTerminated()) {}
    }

    public void pauseTask() {
        setPaused(true);

    }

    public void stopTask() {
        setStopped(true);
    }

    public Boolean getPaused() {
        return isPaused;
    }

    public void setPaused(Boolean paused) {
        isPaused = paused;
    }

    public Boolean getPausedRightNow() {
        return isPausedRightNow;
    }

    public void setPausedRightNow(Boolean pausedRightNow) {
        isPausedRightNow = pausedRightNow;
    }

    public Boolean getStopped() {
        return isStopped;
    }

    public void setStopped(Boolean stopped) {
        isStopped = stopped;
    }
}
