package target;

import exceptions.TargetNotExistException;
import exceptions.TargetsDependsOnEachOtherException;
import xmlfiles.generated.GPUPTarget;
import xmlfiles.generated.GPUPTargetDependencies;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Target implements Serializable {
    public Duration getTargetTaskTime() {
        return targetTaskTime;
    }

    public void setTargetTaskTime(Duration targetTaskTime) {
        this.targetTaskTime = targetTaskTime;
    }

    public Instant getTargetTaskBegin() {
        return targetTaskBegin;
    }

    public void setTargetTaskBegin(Instant targetTaskBegin) {
        this.targetTaskBegin = targetTaskBegin;
    }

    public Instant getTargetTaskEnd() {
        return targetTaskEnd;
    }

    public void setTargetTaskEnd(Instant targetTaskEnd) {
        this.targetTaskEnd = targetTaskEnd;
    }

    public Type getNodeType() {
        return nodeType;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public void addDependencies(List<GPUPTargetDependencies.GPUGDependency> dependencyList, Map<String, Target> allTargets) throws TargetNotExistException, TargetsDependsOnEachOtherException {

        for (GPUPTargetDependencies.GPUGDependency dependency: dependencyList){
            if (!allTargets.containsKey(dependency.getValue()))
                throw new TargetNotExistException(dependency.getValue(),this);
            Target otherTarget = allTargets.get(dependency.getValue());

            if (dependency.getType().equals("dependsOn")) {
                if (otherTarget.dependsOnSet.contains(this)){
                    throw new TargetsDependsOnEachOtherException(this,otherTarget);
                }
                this.dependsOnSet.add(otherTarget);
                otherTarget.requiredForSet.add(this);
            }
            else if (dependency.getType().equals("requiredFor")) {
                if (otherTarget.requiredForSet.contains(this)){
                    throw new TargetsDependsOnEachOtherException(this,otherTarget);
                }
                this.requiredForSet.add(otherTarget);
                otherTarget.dependsOnSet.add(this);
            }
        }

    }

    public boolean isDidSucceedInPrevRuns() {
        return didSucceedInPrevRuns;
    }

    public void setDidSucceedInPrevRuns(boolean didSucceedInPrevRuns) {
        this.didSucceedInPrevRuns = didSucceedInPrevRuns;
    }

    static public enum Status {FROZEN, SKIPPED, WAITING, IN_PROCESS, FINISHED};
    static public enum Result {SUCCESS, WARNING, FAILURE, SKIPPED};
    static public enum Type {LEAF, MIDDLE, ROOT, INDEPENDENT};

    private String name;
    private String ExtraData;
    private Set<Target> requiredForSet;
    private Set<Target> dependsOnSet;
    private Status runStatus;
    private Result runResult;
    private Type nodeType;
    private Duration targetTaskTime;
    private Instant targetTaskBegin,targetTaskEnd;
    private boolean isVisited;
    private boolean didSucceedInPrevRuns;

    public Target(String name, String extraData)
    {
        this.name = name;
        if(extraData != null)
            this.ExtraData = extraData;
        else
            this.ExtraData = "";
        this.dependsOnSet = new HashSet<Target>();
        this.requiredForSet = new HashSet<Target>();
        this.runStatus = Status.WAITING;
        this.resetTarget();
    }

    public Target(String name)
    {
        this(name,"");
    }

    public void resetTarget(){
        this.isVisited = false;
        this.didSucceedInPrevRuns = false;
        determineInitialType();
        this.runResult = Result.SKIPPED;
    }

    public Target(GPUPTarget gpupTarget){
            this(gpupTarget.getName(),gpupTarget.getGPUPUserData());
    }

    public void determineInitialType() {
        if (dependsOnSet.isEmpty() && requiredForSet.isEmpty()) {
            nodeType = Type.INDEPENDENT;
            runStatus = Status.WAITING;
        }
        else if (dependsOnSet.isEmpty() && !requiredForSet.isEmpty()) {
            nodeType = Type.LEAF;
            runStatus = Status.WAITING;
        }
        else if (!dependsOnSet.isEmpty() && requiredForSet.isEmpty()) {
            nodeType = Type.ROOT;
            runStatus = Status.FROZEN;
        }
        else {
            nodeType = Type.MIDDLE;
            runStatus = Status.FROZEN;
        }
    }

    public void determineIfStatusIsWaiting() {
        int successCounter = 0;
        for (Target target : dependsOnSet) {
            if (target.runResult == Result.SUCCESS || target.runResult == Result.WARNING) {
                successCounter++;
            }
        }
        if (successCounter == dependsOnSet.size())
            this.runStatus = Status.WAITING;
    }

    /**
     *
     * @return set of targets that depends on this target.
     */
    public Set<Target> getAllAboveTargets() {
        Set<Target> aboveTargets = new HashSet<>();
        getAllAboveTargetsRec(this,aboveTargets);
        return aboveTargets;
    }

    private void getAllAboveTargetsRec(Target target ,Set<Target> aboveTargets)
    {
        for (Target curTarget: target.getRequiredForSet()) {
            aboveTargets.add(curTarget);
            getAllAboveTargetsRec(curTarget,aboveTargets);
        }
    }

    public void setResult(Result res){
        this.runResult = res;
    }

    public void setStatus(Status st){
        this.runStatus = st;
    }

    public void setAllAboveSkipped(){
        for (Target target: this.getAllAboveTargets()){
            target.setStatus(Status.SKIPPED);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        if(name != null)
            return name.equalsIgnoreCase(target.name);
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Status getRunStatus() {
        return runStatus;
    }

    public String getName() {
        return name;
    }

    public String getExtraData() {
        return ExtraData;
    }

    public Set<Target> getRequiredForSet() {
        return requiredForSet;
    }

    public Set<Target> getDependsOnSet() {
        return dependsOnSet;
    }

    public Result getRunResult() {
        return runResult;
    }
}
