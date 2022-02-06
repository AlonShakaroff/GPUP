package dtos;

import target.Target;
import target.TargetGraph;

import java.util.Map;
import java.util.Set;

public class TaskDetailsDto {

    private final String taskName;
    private final String graphName;
    private final String uploader;
    private final Integer targets;
    private final Integer roots;
    private final Integer middles;
    private final Integer leaves;
    private final Integer independents;
    private final Integer totalPayment;
    private Integer totalWorkers;
    private String taskStatus;

    public TaskDetailsDto(String taskName, String creatorName, TargetGraph targetGraph) {
        this.taskName = taskName;
        this.graphName = targetGraph.getGraphName();
        this.uploader = creatorName;
        this.totalWorkers = 0;
        this.taskStatus = "New";

        Map<String,Target> allTargets = targetGraph.getAllTargets();
        this.roots = (int)allTargets.values().stream().filter(target -> target.getNodeType().equals(Target.Type.ROOT)).count();
        this.middles = (int)allTargets.values().stream().filter(target -> target.getNodeType().equals(Target.Type.MIDDLE)).count();
        this.leaves = (int)allTargets.values().stream().filter(target -> target.getNodeType().equals(Target.Type.LEAF)).count();
        this.independents = (int)allTargets.values().stream().filter(target -> target.getNodeType().equals(Target.Type.INDEPENDENT)).count();
        this.targets = allTargets.size();

        Map<TargetGraph.TaskType, Integer> taskPrices = targetGraph.getTaskPricing();
        this.totalPayment = taskPrices.get(TargetGraph.TaskType.SIMULATION) != null ?
                taskPrices.get(TargetGraph.TaskType.SIMULATION) * this.targets : taskPrices.get(TargetGraph.TaskType.COMPILATION) * this.targets;
    }

    public void addWorker() { this.totalWorkers++; }

    public void removeWorker() { this.totalWorkers--; }

    public void setTaskStatus(String status) { this.taskStatus = status; }

    public String getGraphName() {
        return this.graphName;
    }

    public String getUploader() {
        return this.uploader;
    }

    public Integer getRoots() {
        return this.roots;
    }

    public Integer getMiddles() {
        return this.middles;
    }

    public Integer getLeaves() {
        return this.leaves;
    }

    public Integer getIndependents() {
        return this.independents;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public Integer getTargets() {
        return this.targets;
    }

    public Integer getTotalPayment() {
        return this.totalPayment;
    }

    public Integer getTotalWorkers() {
        return this.totalWorkers;
    }

    public String getTaskStatus() {
        return this.taskStatus;
    }

}
