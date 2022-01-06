package target;

import exceptions.*;
import xmlfiles.generated.GPUPConfiguration;
import xmlfiles.generated.GPUPDescriptor;
import xmlfiles.generated.GPUPTarget;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class TargetGraph implements Serializable {

    private final Map<Target.Status, Set<Target>> statusGraph;
    private final Map<Target.Type, Set<Target>> typeSetMap;
    private final Map<String, Target> allTargets;
    private Duration totalTaskDuration;
    private Instant taskStartTime, taskEndTime;
    private Boolean canRunIncrementally;
    private String graphName;
    private String directory;
    private Map<String, Set<String>> SerialSets;
    private int maxParallelism;

    public Map<String, Set<String>> getSerialSets() {
        return SerialSets;
    }

    static public enum pathDirection {DEPENDS_ON, REQUIRED_FOR}

    public TargetGraph(String name, String directory, int maxParallelism) {
        this.graphName = name;
        this.directory = directory;
        this.maxParallelism = maxParallelism;
        this.SerialSets = new HashMap<>();
        allTargets = new HashMap<>();
        statusGraph = new HashMap<>();
        typeSetMap = new HashMap<>();
        statusGraph.put(Target.Status.WAITING, new HashSet<>());
        statusGraph.put(Target.Status.FINISHED, new HashSet<>());
        statusGraph.put(Target.Status.FROZEN, new HashSet<>());
        statusGraph.put(Target.Status.IN_PROCESS, new HashSet<>());
        statusGraph.put(Target.Status.SKIPPED, new HashSet<>());
        typeSetMap.put(Target.Type.INDEPENDENT, new HashSet<>());
        typeSetMap.put(Target.Type.LEAF, new HashSet<>());
        typeSetMap.put(Target.Type.MIDDLE, new HashSet<>());
        typeSetMap.put(Target.Type.ROOT, new HashSet<>());
        canRunIncrementally = false;
    }

    public void InitializeTypes() {
        for (Target target : allTargets.values()) {
            target.resetTarget();
            statusGraph.get(target.getRunStatus()).add(target);
            typeSetMap.get(target.getNodeType()).add(target);
        }
    }

    public void resetGraph() {
        for (Target target : allTargets.values()) {
            target.resetTarget();
            statusGraph.get(target.getRunStatus()).add(target);
        }
        canRunIncrementally = false;
    }

    public void recalculateStatusGraph() {
        clearStatusGraph();
        for (Target target : allTargets.values())
            statusGraph.get(target.getRunStatus()).add(target);
    }

    public void clearStatusGraph() {
        statusGraph.get(Target.Status.WAITING).clear();
        statusGraph.get(Target.Status.FINISHED).clear();
        statusGraph.get(Target.Status.FROZEN).clear();
        statusGraph.get(Target.Status.IN_PROCESS).clear();
        statusGraph.get(Target.Status.SKIPPED).clear();
    }

    public void addTargetToGraph(Target target) {
        target.determineInitialType();
        allTargets.put(target.getName().toUpperCase(), target);
    }

    public void MoveTargetTo(Target target, Target.Status from, Target.Status to) {
        statusGraph.get(from).remove(target);
        statusGraph.get(to).add(target);
    }


    /**
     * Checks if the task finished running on all the target graph, all targets are frozen or finished.
     *
     * @return
     */
    public boolean isTaskFinished() {
        return (statusGraph.get(Target.Status.IN_PROCESS).isEmpty() &&
                statusGraph.get(Target.Status.WAITING).isEmpty() &&
                statusGraph.get(Target.Status.FROZEN).isEmpty());
    }

    public boolean didAllTargetsSucceed(){
        return allTargets.values().stream().allMatch(target -> (target.getRunResult() == Target.Result.SUCCESS ||  target.getRunResult() == Target.Result.WARNING));
    }

    public Set<Target> getWaitingSet() {
        return statusGraph.get(Target.Status.WAITING);
    }

    public Set<Target> getSetByType(Target.Type type) {
        return typeSetMap.get(type);
    }

    public Map<String, Target> getAllTargets() {
        return allTargets;
    }

    public Duration getTotalTaskDuration() {
        return totalTaskDuration;
    }

    public void setTotalTaskDuration(Duration totalTaskDuration) {
        this.totalTaskDuration = totalTaskDuration;
    }

    public Instant getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(Instant taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public Instant getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Instant taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public Boolean getCanRunIncrementally() {
        return canRunIncrementally;
    }

    public void canRunIncrementally() {
        if (didATargetFail())
            this.canRunIncrementally = true;
        else
            this.canRunIncrementally = false;
    }

    private boolean didATargetFail() {
        for (Target target : allTargets.values()) {
            if (target.getRunResult() == Target.Result.FAILURE)
                return true;
        }
        return false;
    }

    public void prepareGraphForIncremental() {
        for (Target target : allTargets.values()) {
            if (target.getRunResult() == Target.Result.FAILURE) {
                MoveTargetTo(target, Target.Status.FINISHED, Target.Status.WAITING);
                target.setStatus(Target.Status.WAITING);
            } else if (target.getRunResult() == Target.Result.SKIPPED) {
                MoveTargetTo(target, Target.Status.SKIPPED, Target.Status.FROZEN);
                target.setStatus(Target.Status.FROZEN);
            }
            else{
                target.setDidSucceedInPrevRuns(true);
            }
        }
    }

    public Target getTarget(String targetName) {
        return allTargets.get(targetName.toUpperCase());
    }

    public Set<List<Target>> getAllPathsFromTwoTargets(Target fromTarget, Target toTarget, pathDirection direction) {
        Set<List<Target>> pathsSet = new HashSet<>();
        List<Target> currentPath = new ArrayList<>();
        getAllPathsFromTwoTargetsRec(fromTarget,toTarget,direction,pathsSet,currentPath);
        return pathsSet;
    }
    private void getAllPathsFromTwoTargetsRec(Target fromTarget, Target toTarget,pathDirection direction, Set<List<Target>> pathsSet,  List<Target> currentPath)
    {
        if(fromTarget.isVisited())
            return;

        currentPath.add(fromTarget);

        if(fromTarget.equals(toTarget)) {
            pathsSet.add(new ArrayList<>(currentPath));
        }
        else {
            fromTarget.setVisited(true);
            if(direction == pathDirection.DEPENDS_ON) {
                for (Target target : fromTarget.getDependsOnSet()) {
                    getAllPathsFromTwoTargetsRec(target, toTarget, direction, pathsSet, currentPath);
                }
            }
            else {
                for (Target target : fromTarget.getRequiredForSet()) {
                    getAllPathsFromTwoTargetsRec(target, toTarget, direction, pathsSet, currentPath);
                }
            }
            fromTarget.setVisited(false);
        }

        currentPath.remove(currentPath.size()-1); // remove last added target from path
    }

    public List<Target> checkIfATargetIsInACircleAndReturnCircle(Target target)
    {
        List<Target> currentPath = new ArrayList<>();

        boolean circleFound = checkIfATargetIsInACircleAndReturnCircleRec(target,target,currentPath);
        if(!circleFound)
            return null;

        return currentPath;
    }

    private boolean checkIfATargetIsInACircleAndReturnCircleRec(Target fromTarget, Target curTarget,List<Target> currentPath)
    {
        if(curTarget.equals(fromTarget)&&curTarget.isVisited()) {
                currentPath.add(curTarget);
                return true;
        }

        if(curTarget.isVisited())
            return false;

        currentPath.add(curTarget);

        curTarget.setVisited(true);

        boolean circleFound = false;
        for (Target target: curTarget.getRequiredForSet() ) {
            if(!circleFound) {
                circleFound = checkIfATargetIsInACircleAndReturnCircleRec(fromTarget,target,currentPath);
            }
        }
        if(!circleFound)
            currentPath.remove(currentPath.size()-1); // remove last added target from path
        curTarget.setVisited(false);
        return circleFound;
    }

    public static TargetGraph createTargetGraphFromXml(File file) throws Exception {

        JAXBContext jaxbContext = JAXBContext.newInstance(GPUPDescriptor.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        GPUPDescriptor gpupDescriptor = (GPUPDescriptor) jaxbUnmarshaller.unmarshal(file);

        GPUPConfiguration gpupConfiguration = gpupDescriptor.getGPUPConfiguration();
        String GpupWorkingDirectory = gpupConfiguration.getGPUPWorkingDirectory();
        checkIfPathIsValidDirectory(GpupWorkingDirectory);
        TargetGraph targetGraph = new TargetGraph(gpupConfiguration.getGPUPGraphName(),GpupWorkingDirectory,gpupConfiguration.getGPUPMaxParallelism());
        for (GPUPTarget gpupTarget : gpupDescriptor.getGPUPTargets().getGPUPTarget()) {
            Target target = new Target(gpupTarget);
            if (targetGraph.allTargets.containsKey(target.getName().toUpperCase())){
                throw new TargetAppearTwiceException(target);
            }
            targetGraph.addTargetToGraph(target);
        }
        if(gpupDescriptor.getGPUPSerialSets() == null)
            targetGraph.SerialSets = null;
        else {
            for (GPUPDescriptor.GPUPSerialSets.GPUPSerialSet serialSet : gpupDescriptor.getGPUPSerialSets().getGPUPSerialSet()) {

                Map<String, Set<String>> serialSets = targetGraph.getSerialSets();
                if (serialSets.containsKey(serialSet.getName().toUpperCase()))
                    throw new TwoSerialSetsWithSameName(serialSet.getName());
                else {
                    String[] targetsInCurrentSet = serialSet.getTargets().split(",");
                    Set<String> currentSetTargetsSet = new HashSet<>();

                    for (String targetName : targetsInCurrentSet) {
                        if (!targetGraph.allTargets.containsKey(targetName.toUpperCase()))
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
                targetGraph.getTarget(gpupTarget.getName()).addDependencies(gpupTarget.getGPUPTargetDependencies().getGPUGDependency(), targetGraph.allTargets);
            }
        }
        targetGraph.InitializeTypes();
        return targetGraph;
    }

    public static void checkIfPathIsValidDirectory(String directoryPath) throws Exception {
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

    public String getDirectory() {
        return directory;
    }

    public int getAmountOfTargets() {
        return allTargets.size();
    }

    public int getAmountOfRoots() {
        return typeSetMap.get(Target.Type.ROOT).size();
    }

    public int getAmountOfMiddles() {
        return typeSetMap.get(Target.Type.MIDDLE).size();
    }

    public int getAmountOfLeafs() {
        return typeSetMap.get(Target.Type.LEAF).size();
    }

    public int getAmountOfIndependent() { return typeSetMap.get(Target.Type.INDEPENDENT).size(); }

    public ArrayList<String> getAllPathsFromTwoTargetsAsStrings (String source, String destination, pathDirection direction) {
        Set<List<Target>> paths = getAllPathsFromTwoTargets(getTarget(source),getTarget(destination),direction);
        ArrayList<String> stringPaths = new ArrayList<>();
        for (List<Target> path: paths) {
            stringPaths.add(returnPathAsString(path));
        }
        return stringPaths;
    }

    public String checkIfTargetIsInACircleAndReturnCircleAsString(Target target) {
        List<Target> circle = checkIfATargetIsInACircleAndReturnCircle(target);
        if(circle == null)
            return "Target is not in a circle";
        else
            return returnPathAsString(circle);
    }

    private String returnPathAsString(List<Target> path) {
        String StringPath = "";
        char rightArrow = '\u2192';
        int i = 0;
        for (Target target : path) {
            if(i==0)
                StringPath += target.getName().toUpperCase();
            else
                StringPath = StringPath + ' ' + rightArrow + ' ' + target.getName().toUpperCase();
            i++;
        }
        return StringPath;
    }
}
