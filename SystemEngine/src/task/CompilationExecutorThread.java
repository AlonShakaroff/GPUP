package task;

import target.Target;
import target.TargetGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompilationExecutorThread extends Thread{
    private TargetGraph targetGraph;
    private List<CompilationTask> tasksList;
    private String taskName;
    private String SourceFolderPath;
    private String DestFolderPath;
    private ExecutorService threadExecutor;
    private Iterator<CompilationTask> iterator;

    public CompilationExecutorThread(TargetGraph targetGraph, String taskName,String SourceFolderPath, String DestFolderPath,int numOfThreads){
        this.targetGraph = targetGraph;
        this.taskName = taskName;
        this.DestFolderPath = DestFolderPath;
        this.SourceFolderPath = SourceFolderPath;
        this.tasksList = new ArrayList<>();
        this.threadExecutor = Executors.newFixedThreadPool(numOfThreads);
        this.iterator = tasksList.iterator();
    }

    @Override
    public void run(){
       while (!targetGraph.isTaskFinished()){
           for (Target target: targetGraph.getWaitingSet()){
                   tasksList.add(new CompilationTask(taskName,SourceFolderPath,DestFolderPath,target));
           }

       }
    }
}
