package task.copilation;

import java.io.File;

public class CompilationParameters {
    private final File sourceCodeDirectory;
    private final File destinationDirectory;

    public CompilationParameters(File sourceCodeDirectory, File outputDirectory) {
        this.sourceCodeDirectory = sourceCodeDirectory;
        this.destinationDirectory = outputDirectory;
    }

    public File getSourceCodeDirectory() {
        return this.sourceCodeDirectory;
    }

    public File getDestinationDirectory() {
        return this.destinationDirectory;
    }
}
