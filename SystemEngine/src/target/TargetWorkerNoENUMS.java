package target;

public class TargetWorkerNoENUMS {
    private String name;
    private String result;
    private String status;
    private String runLog;
    private String extraData;
    private String taskName;
    private Integer pricing;


    public TargetWorkerNoENUMS(TargetForWorker targetForWorker) {
        this.name = targetForWorker.getName();
        this.extraData = targetForWorker.getExtraData();
        this.runLog = targetForWorker.getRunLog();
        this.taskName = targetForWorker.getTaskName();
        this.pricing = targetForWorker.getPricing();
        this.result = "in process";
        this.status = "waiting";
    }

    public String getName() {
        return name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getRunLog() {
        return runLog;
    }

    public void setRunLog(String runLog) {
        this.runLog = runLog;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExtraData() {
        return extraData;
    }

    public String getTaskName() {
        return taskName;
    }

    public Integer getPricing() {
        return pricing;
    }
}
