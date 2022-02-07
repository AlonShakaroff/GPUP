package task.copilation;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import target.Target;
import target.TargetGraph;
import task.ExecutorThread;
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
                           Target target, ExecutorThread taskManager, TextArea runLogTextArea) {
        super(taskName, target,taskManager,runLogTextArea);
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
            catch(Exception Ignore){}
        }

        String FQNToPath = "\\" + target.getExtraData().replace(".","\\");
        String javaFilePath = sourceFolderPath + FQNToPath + ".java";
        try {
            target.setStatus(Target.Status.IN_PROCESS);
            target.setStartTimeInCurState();
            target.setTargetTaskBegin(Instant.now());

            Platform.runLater(()->{runLogTextArea.appendText("Target " + target.getName() + " is starting compilation\n\n"); });

            ProcessBuilder processBuilder = new ProcessBuilder("javac", "-d", destinationPath, "-cp", destinationPath, javaFilePath);
            Process process;

            process = processBuilder.start();
            int code = process.waitFor();

            if (code == 0) {
                target.setResult(Target.Result.SUCCESS);
                Platform.runLater(()->{runLogTextArea.appendText("Target " + target.getName() + " compiled successfully with compilation time of: " +
                        TargetGraph.getDurationAsString(Duration.between(target.getTargetTaskBegin(), Instant.now())) + "\n\n"); });
            } else {
                target.setResult(Target.Result.FAILURE);
                String errorMsg = "";
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                for (String errorLine:bufferedReader.lines().collect(Collectors.toList())) {
                    errorMsg += errorLine + "\n";
                }
                String finalErrorMsg = errorMsg;
                Platform.runLater(()->{runLogTextArea.appendText("Target " + target.getName() + " compilation failed!\n" +
                        "error message:\n" + finalErrorMsg + "\n\n"); });

            }
            target.setStatus(Target.Status.FINISHED);
        }catch (Exception exception) {
            Platform.runLater(()->{runLogTextArea.appendText("Target " + target.getName() + " was interrupted!\n\n"); });
            target.setStatus(Target.Status.SKIPPED);
            target.setResult(Target.Result.SKIPPED);
        }
    }
}