package target;

import exceptions.TargetAppearTwiceException;
import exceptions.TargetThatAppearsInTheSerialSetDoNotExist;
import exceptions.TwoSerialSetsWithSameName;
import exceptions.pathIsNotDirException;
import xmlfiles.generated.GPUPConfiguration;
import xmlfiles.generated.GPUPDescriptor;
import xmlfiles.generated.GPUPTarget;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileChecker {
    public TargetGraph createTargetGraphFromXml(File file) throws Exception {

        JAXBContext jaxbContext = JAXBContext.newInstance(GPUPDescriptor.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        GPUPDescriptor gpupDescriptor = (GPUPDescriptor) jaxbUnmarshaller.unmarshal(file);

        GPUPConfiguration gpupConfiguration = gpupDescriptor.getGPUPConfiguration();
        String GpupWorkingDirectory = gpupConfiguration.getGPUPWorkingDirectory();
        checkIfPathIsValidDirectory(GpupWorkingDirectory);
        TargetGraph targetGraph = new TargetGraph(gpupConfiguration.getGPUPGraphName(),GpupWorkingDirectory,gpupConfiguration.getGPUPMaxParallelism());
        for (GPUPTarget gpupTarget : gpupDescriptor.getGPUPTargets().getGPUPTarget()) {
            Target target = new Target(gpupTarget);
            if (targetGraph.getAllTargets().containsKey(target.getName().toUpperCase())){
                throw new TargetAppearTwiceException(target);
            }
            targetGraph.addTargetToGraph(target);
        }
        if(gpupDescriptor.getGPUPSerialSets() == null)
            targetGraph.setSerialSets(null);
        else {
            for (GPUPDescriptor.GPUPSerialSets.GPUPSerialSet serialSet : gpupDescriptor.getGPUPSerialSets().getGPUPSerialSet()) {

                Map<String, Set<String>> serialSets = targetGraph.getSerialSets();
                if (serialSets.containsKey(serialSet.getName().toUpperCase()))
                    throw new TwoSerialSetsWithSameName(serialSet.getName());
                else {
                    String[] targetsInCurrentSet = serialSet.getTargets().split(",");
                    Set<String> currentSetTargetsSet = new HashSet<>();

                    for (String targetName : targetsInCurrentSet) {
                        if (!targetGraph.getAllTargets().containsKey(targetName.toUpperCase()))
                            throw new TargetThatAppearsInTheSerialSetDoNotExist(targetName.toUpperCase(), serialSet.getName().toUpperCase());
                        else {
                            targetGraph.getTarget(targetName).addSerialSet(serialSet.getName());
                            currentSetTargetsSet.add(targetName.toUpperCase());
                        }
                    }
                    serialSets.put(serialSet.getName(), currentSetTargetsSet);
                }
            }
        }
        for (GPUPTarget gpupTarget : gpupDescriptor.getGPUPTargets().getGPUPTarget()) {
            if (gpupTarget.getGPUPTargetDependencies() != null) {
                targetGraph.getTarget(gpupTarget.getName()).addDependencies(gpupTarget.getGPUPTargetDependencies().getGPUGDependency(), targetGraph.getAllTargets());
            }
        }
        targetGraph.resetGraph();
        return targetGraph;
    }

    public void checkIfPathIsValidDirectory(String directoryPath) throws Exception {
        if (directoryPath == null)
            return;
        Path workingDirPath = new File(directoryPath).toPath();
        if(Files.exists(workingDirPath)) {
            if(!Files.isDirectory(workingDirPath))
                throw new pathIsNotDirException(directoryPath);
        }
        else {
            Files.createDirectories(workingDirPath);
        }
    }
}
