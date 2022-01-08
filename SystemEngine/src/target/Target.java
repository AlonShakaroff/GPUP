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
    private Set<String> serialSets;

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
        this.serialSets = new HashSet<>();
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

    public Set<String> getSerialSets() {
        return serialSets;
    }

    public void addSerialSet(String setName) { serialSets.add(setName); }

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

    public String getNodeTypeAsString() {
        switch (nodeType)
        {
            case LEAF:
                return "Leaf";
            case ROOT:
                return "Root";
            case MIDDLE:
                return "Middle";
            case INDEPENDENT:
                return "Independent";
        }
        return "";
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

    public boolean didSucceedInPrevRuns() {
        return didSucceedInPrevRuns;
    }

    public void setDidSucceedInPrevRuns(boolean didSucceedInPrevRuns) {
        this.didSucceedInPrevRuns = didSucceedInPrevRuns;
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
    public Set<Target> getAllRequiredForTargets() {
        Set<Target> aboveTargets = new HashSet<>();
        getAllRequiredForTargetsRec(this,aboveTargets);
        return aboveTargets;
    }

    private void getAllRequiredForTargetsRec(Target target , Set<Target> aboveTargets)
    {
        for (Target curTarget: target.getRequiredForSet()) {
            if(!aboveTargets.contains(curTarget)) {
                aboveTargets.add(curTarget);
                getAllRequiredForTargetsRec(curTarget, aboveTargets);
            }
        }
    }

    public Set<String> getAllRequiredForTargetsAsStrings() {
        Set<String> requiredForTargets = new HashSet<>();
        for(Target target: getAllRequiredForTargets()) {
            requiredForTargets.add(target.getName().toUpperCase());
        }
        return requiredForTargets;
    }

    public Set<Target> getAllDependsOnTargets() {
        Set<Target> belowTargets = new HashSet<>();
        getAllDependsOnTargetsRec(this,belowTargets);
        return belowTargets;
    }

    private void getAllDependsOnTargetsRec(Target target , Set<Target> belowTargets)
    {
        for (Target curTarget: target.getDependsOnSet()) {
            if(!belowTargets.contains(curTarget)) {
                belowTargets.add(curTarget);
                getAllDependsOnTargetsRec(curTarget, belowTargets);
            }
        }
    }

    public Set<String> getAllDependsOnTargetsAsStrings() {
        Set<String> dependsOnTargets = new HashSet<>();
        for(Target target: getAllDependsOnTargets()) {
            dependsOnTargets.add(target.getName().toUpperCase());
        }
        return dependsOnTargets;
    }

    public void setResult(Result res){
        this.runResult = res;
    }

    public void setStatus(Status st){
        this.runStatus = st;
    }

    public void setAllAboveSkipped(){
        for (Target target: this.getAllRequiredForTargets()){
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

    public int getAmountOfDirectlyDependsOn() {
        return getDependsOnSet().size();
    }

    public int getAmountOfTotalDependsOn() {
        return getAllDependsOnTargets().size();
    }

    public int getAmountOfDirectlyRequiredFor() {
        return getRequiredForSet().size();
    }

    public int getAmountOfTotalRequiredFor() {
        return getAllRequiredForTargets().size();
    }

    public int getAmountOfSerialSets() {
        return getSerialSets().size();
    }

    public String getRunStatusAsString() {
        switch(runStatus) {
            case WAITING:
                return "Waiting";
            case FROZEN:
                return "Frozen";
            case SKIPPED:
                return "Skipped";
            case FINISHED:
                return "Finished";
            case IN_PROCESS:
                return "In process";
        }
        return "";
    }
}
