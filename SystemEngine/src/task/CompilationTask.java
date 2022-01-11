package task;

import com.sun.net.httpserver.Authenticator;
import target.Target;
import target.TargetGraph;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

public class CompilationTask extends GPUPTask {

    private final String sourceFolderPath;
    private String destinationPath;

    public CompilationTask(String taskName, String sourceFilePath, String destinationFilePath, Target target,ExecutorThread taskManager) {
        super(taskName, target,taskManager);
        this.sourceFolderPath = sourceFilePath;
        this.destinationPath = destinationFilePath;
    }

    @Override
    public void run() {
        synchronized (this.taskManager.getIsPauseDummy()){
            try {
                while(this.taskManager.getPaused()) {
                    this.taskManager.getIsPauseDummy().wait();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        destinationPath = destinationPath.replace(" ", "^ ");
        target.setStatus(Target.Status.IN_PROCESS);
        target.setTargetTaskBegin(Instant.now());
        String FQNToPath = "\\" + target.getExtraData().replace(".","\\");
        String javaFilePath = sourceFolderPath + FQNToPath + ".java";
        javaFilePath = javaFilePath.replace(" ", "^ ");
        try {
            System.out.println("Target " + target.getName() + " is starting compilation\n\n");
            this.taskManager.getTargetGraph().currentTaskLog += "Target " + target.getName() + " is starting compilation\n\n";
            Process process = Runtime.getRuntime().exec("cmd /c start /wait " + " javac -d " + destinationPath
                    + " -cp " + destinationPath + " " + javaFilePath);
            process.waitFor();
            target.setResult(Target.Result.SUCCESS);
            System.out.println("Target " + target.getName() + " compiled successfully with compilation time of: " +
                    TargetGraph.getDurationAsString(Duration.between(target.getTargetTaskBegin(),Instant.now())) + "\n\n");
            this.taskManager.getTargetGraph().currentTaskLog += "Target " + target.getName() + " compiled successfully with compilation time of: " +
                    TargetGraph.getDurationAsString(Duration.between(target.getTargetTaskBegin(),Instant.now())) + "\n\n";

        }catch (Exception exception) {
            target.setResult(Target.Result.FAILURE);
            System.out.println("Target " + target.getName() + " compilation failed\n\n");
            this.taskManager.getTargetGraph().currentTaskLog += "Target " + target.getName() + " compilation failed\n\n";
        }
        finally {
            target.setStatus(Target.Status.FINISHED);
            target.setTargetTaskEnd(Instant.now());
            target.setTargetTaskTime(Duration.between(target.getTargetTaskBegin(),target.getTargetTaskEnd()));
        }
    }
}
