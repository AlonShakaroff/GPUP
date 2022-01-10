package task;

import target.Target;
import target.TargetGraph;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class SimulationExecutorThread extends Thread{
    private TargetGraph targetGraph;
    private LinkedList<SimulationTask> tasksList;
    private String taskName;
    private double warningChance;
    private double successChance;
    private boolean isRandom;
    private int processTimeInMS;
    private ExecutorService threadExecutor;

    public SimulationExecutorThread(TargetGraph targetGraph, String taskName, double warningChance, double successChance,
                                    boolean isRandom, int processTimeInMS, int numOfThreads, boolean isIncremental){
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
    private void initTasksList(boolean isIncremental){
        for(Target target : targetGraph.getTargetsToRunOn(isIncremental)) {
            if (target.getRunStatus().equals(Target.Status.WAITING)) {
                tasksList.addFirst(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance, target));
            } else {
                tasksList.addLast(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance, target));
            }
        }
    }

    @Override
    public void run(){

       while (!tasksList.isEmpty()){
           SimulationTask curTask = tasksList.poll();
           if (curTask.target.getRunStatus().equals(Target.Status.FROZEN)) { ///target is frozen
               tasksList.addLast(curTask);
           }
           else{    /// target is waiting (but maybe needs to be skipped)
               curTask.getTarget().checkIfNeedsToBeSkipped();
               if (curTask.getTarget().getRunStatus().equals(Target.Status.SKIPPED)){
                   System.out.println("Target " + curTask.getTarget().getName() +
                           " is skipped because " + curTask.getTarget().getResponsibleTargets().toString() + "failed \n");
               }
               else{  // target is waiting to run!
                   System.out.println("Adding task on target " + curTask.getTarget().getName() + " to thread pool.");
                   threadExecutor.execute(curTask);
               }
           }
           targetGraph.refreshWaiting();
       }
       try {
           while (!threadExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
           }
           threadExecutor.shutdown();
       }
       catch(InterruptedException e) {
           threadExecutor.shutdown();
       }
    }
}
