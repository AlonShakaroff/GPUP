package worker.taskmanagment;

public class TaskHistoryDto {
    private String taskName;
    private Integer targetsDoneByWorker;
    private Integer creditsEarned;

    public TaskHistoryDto(String taskName){
        this.taskName = taskName;
        this.targetsDoneByWorker = 0;
        this.creditsEarned = 0;
    }

    public String getTaskName() {
        return taskName;
    }

    public Integer getTargetsDoneByWorker() {
        return targetsDoneByWorker;
    }

    public Integer getCreditsEarned() {
        return creditsEarned;
    }

    public void addCredits(Integer credits) {
        this.creditsEarned += credits;
    }

    public void addTargetDone() {
        System.out.println(targetsDoneByWorker + " target is done!");
        this.targetsDoneByWorker = this.targetsDoneByWorker + 1;
    }
}
