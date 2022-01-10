package task;

import com.sun.jmx.snmp.tasks.Task;
import javafx.scene.shape.Path;
import target.Target;
import target.TargetGraph;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class ExecutorThread extends Thread{
    private TargetGraph targetGraph;
    private LinkedList<GPUPTask> tasksList;
    private String taskName;
    /*---------------------Simulation parameters-----------------------------*/
    private double warningChance;
    private double successChance;
    private boolean isRandom;
    private int processTimeInMS;
    private int numOfThreads;
    private ExecutorService threadExecutor;
    /*---------------------Compilation parameters----------------------------*/
    private String SourceFolderPath;
    private String DestFolderPath;
    /*-----------------------------------------------------------------------*/
    private Boolean isPaused = false;
    private Boolean isStopped = false;
    /*-----------------------------------------------------------------------*/

    public ExecutorThread(TargetGraph targetGraph, String taskName, double warningChance, double successChance,
                          boolean isRandom, int processTimeInMS, int numOfThreads, boolean isIncremental){
        this.targetGraph = targetGraph;
        this.taskName = taskName;
        this.warningChance = warningChance;
        this.successChance = successChance;
        this.isRandom = isRandom;
        this.processTimeInMS = processTimeInMS;
        this.tasksList = new LinkedList<>();
        this.numOfThreads = numOfThreads;
        this.threadExecutor = Executors.newFixedThreadPool(numOfThreads);
        initTasksList(isIncremental);
    }

    public ExecutorThread(TargetGraph targetGraph, String taskName,String SourceFolderPath, String DestFolderPath,int numOfThreads, boolean isIncremental) {
        this.isStopped = false;
        this.targetGraph = targetGraph;
        this.taskName = taskName;
        this.SourceFolderPath = SourceFolderPath;
        this.DestFolderPath = DestFolderPath;
        this.numOfThreads = numOfThreads;
        this.threadExecutor = Executors.newFixedThreadPool(numOfThreads);
        initTasksList(isIncremental);
    }
    private void initTasksList(boolean isIncremental){
        tasksList.clear();
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
    public void run() {
        targetGraph.setTaskStartTime(Instant.now());
        while (!tasksList.isEmpty()) {
            if (isStopped) { // break if stopped
                threadExecutor.shutdownNow();
                break;
            }
            if (isPaused) { // while paused
                System.out.println("paused");
                threadExecutor.shutdownNow();
                while (isPaused) {}
                initTasksList(true);
                threadExecutor = Executors.newFixedThreadPool(numOfThreads);
            }
            GPUPTask curTask = tasksList.poll();
            if (curTask.target.getRunStatus().equals(Target.Status.FROZEN)) { // target is frozen
                tasksList.addLast(curTask);
            } else {    // target is waiting (but maybe needs to be skipped)
                curTask.getTarget().checkIfNeedsToBeSkipped();
                if (curTask.getTarget().getRunStatus().equals(Target.Status.SKIPPED)) {
                    System.out.println("Target " + curTask.getTarget().getName() +
                            " is skipped because " + curTask.getTarget().getResponsibleTargets().toString() + "failed \n");
                } else {  // target is waiting to run, but maybe can't run due to a serial set
                    if (targetGraph.DoesHaveSerialMemberInProgress(curTask.target))
                        tasksList.addLast(curTask);
                    else {   // target is waiting to run!!!
                        System.out.println("Adding task on target " + curTask.getTarget().getName() + " to thread pool.");
                        threadExecutor.submit(curTask);
                    }
                }
            }
            targetGraph.refreshWaiting();
        }
        shutdown();
        targetGraph.setTaskEndTime(Instant.now());
    }

    public void shutdown() {
        threadExecutor.shutdown();
        while(!threadExecutor.isTerminated()) {}
    }

    public Boolean getPaused() {
        return isPaused;
    }

    public void setPaused(Boolean paused) {
        isPaused = paused;
    }

    public Boolean getStopped() {
        return isStopped;
    }

    public void setStopped(Boolean stopped) {
        isStopped = stopped;
    }
}
