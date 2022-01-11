package task;

import com.sun.net.httpserver.Authenticator;
import target.Target;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

public class CompilationTask extends GPUPTask {

    private final String sourceFolderPath;
    private final String destinationPath;

    public CompilationTask(String taskName, String sourceFilePath, String destinationFilePath, Target target,ExecutorThread taskManager) {
        super(taskName, target,taskManager);
        this.sourceFolderPath = sourceFilePath;
        this.destinationPath = destinationFilePath;
    }

    @Override
    public void run() {
        target.setStatus(Target.Status.IN_PROCESS);
        target.setTargetTaskBegin(Instant.now());
        String FQNToPath = "\\" + target.getExtraData().replace(".","\\");
        String javaFilePath = sourceFolderPath + FQNToPath + ".java";
        try {
            System.out.println("target " + target.getName() + "is starting compilation");
            Process process = Runtime.getRuntime().exec("cmd /c start /wait " + " javac -d " + destinationPath
                    + " -cp " + destinationPath + " " + javaFilePath + " && exit");
            process.waitFor();
            target.setResult(Target.Result.SUCCESS);
            System.out.println("task succeeded with compilation time of: " + Duration.between(target.getTargetTaskBegin(),Instant.now()));
        }catch (Exception exception) {
            target.setResult(Target.Result.FAILURE);
            System.out.println();
            System.out.println("task Failed");
        }
        finally {
            target.setStatus(Target.Status.FINISHED);
            target.setTargetTaskEnd(Instant.now());
            target.setTargetTaskTime(Duration.between(target.getTargetTaskBegin(),target.getTargetTaskEnd()));
        }
    }
}
