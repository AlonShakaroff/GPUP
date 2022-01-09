package task;

import target.Target;

import java.io.File;

public class CompilationTask extends Task {

    private final File sourceFolder;
    private final File destinationFolder;

    public CompilationTask(String taskName, String sourceFilePath, String destinationFilePath, Target target) {
        super(taskName, target);
        sourceFolder = new File(sourceFilePath);
        destinationFolder = new File(destinationFilePath);
    }

    @Override
    public void run() {

    }
}
