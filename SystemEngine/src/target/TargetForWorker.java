package target;

public class TargetForWorker {
    private String name;
    private Target.Result result;
    private Target.Status status;
    private String runLog;
    private String extraData;
    private String taskName;
    private Integer pricing;


    public TargetForWorker(String name, String extraData, String taskName, Integer pricing) {
        this.name = name;
        this.extraData = extraData;
        this.runLog = "";
        this.taskName = taskName;
        this.pricing = pricing;
    }

    public String getName() {
        return name;
    }

    public Target.Result getTargetResult() {
        return result;
    }

    public void setResult(Target.Result result) {
        this.result = result;
    }

    public String getResult() {
        return this.result.name();
    }

    public String getStatus() {
        return this.status.name();
    }

    public String getRunLog() {
        return runLog;
    }

    public void setRunLog(String runLog) {
        this.runLog = runLog;
    }

    public Target.Status getTargetStatus() {
        return status;
    }

    public void setStatus(Target.Status status) {
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
