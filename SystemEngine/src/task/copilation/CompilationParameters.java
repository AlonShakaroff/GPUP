package task.copilation;

import java.io.File;

public class CompilationParameters {
    private final File sourceCodeDirectory;
    private final File destinationDirectory;
    private String ExtraData;

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

    public String getExtraData() {
        return ExtraData;
    }

    public void setExtraData(String extraData) {
        ExtraData = extraData;
    }
}
