package task.copilation;

import java.io.File;

public class CompilationParameters {
    private final File sourceCodeDirectory;
    private final File destinationDirectory;

    public CompilationParameters(File sourceCodeDirectory, File destinationDirectory) {
        this.sourceCodeDirectory = sourceCodeDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    public File getSourceCodeDirectory() {
        return this.sourceCodeDirectory;
    }

    public File getDestinationDirectory() {
        return this.destinationDirectory;
    }
}
