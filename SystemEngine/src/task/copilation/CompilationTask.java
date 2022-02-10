package task.copilation;

import target.Target;
import target.TargetForWorker;
import target.TargetGraph;
import task.GPUPTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

public class CompilationTask extends GPUPTask {

    private final String sourceFolderPath;
    private String destinationPath;

    public CompilationTask(String taskName, String sourceFilePath, String destinationFilePath,
                           TargetForWorker target) {
        super(taskName, target, "Compilation");
        this.sourceFolderPath = sourceFilePath;
        this.destinationPath = destinationFilePath;
    }

    @Override
    public void run() {

        String FQNToPath = "\\" + target.getExtraData().replace(".","\\");
        String javaFilePath = sourceFolderPath + FQNToPath + ".java";
        try {
            Instant targetTaskBegin = Instant.now();
            target.setStatus(Target.Status.IN_PROCESS);
            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " is starting compilation\n\n"));

            ProcessBuilder processBuilder = new ProcessBuilder("javac", "-d", destinationPath, "-cp", destinationPath, javaFilePath);
            Process process;

            process = processBuilder.start();
            int code = process.waitFor();

            if (code == 0) {
                target.setResult(Target.Result.SUCCESS);
                target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " compiled successfully with compilation time of: " +
                        TargetGraph.getDurationAsString(Duration.between(targetTaskBegin, Instant.now())) + "\n\n"));
            } else {
                target.setResult(Target.Result.FAILURE);
                String errorMsg = "";
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                for (String errorLine:bufferedReader.lines().collect(Collectors.toList())) {
                    errorMsg += errorLine + "\n";
                }
                String finalErrorMsg = errorMsg;
                target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " compilation failed!\n" +
                        "error message:\n" + finalErrorMsg + "\n\n"));

            }
            target.setStatus(Target.Status.FINISHED);
        }catch (Exception exception) {
            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " was interrupted!\n\n"));
            target.setStatus(Target.Status.SKIPPED);
            target.setResult(Target.Result.SKIPPED);
        }
        finally{
            uploadTaskResultToServer();
        }
    }
}
