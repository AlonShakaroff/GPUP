package target;

import java.util.Locale;

public class TargetForWorker {
    private String name;
    private String result;
    private String status;
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

    public Target.Result getResult() {
        return Target.Result.valueOf(result);
    }

    public void setResult(Target.Result result) {
        this.result = result.toString();
    }

    public String getRunLog() {
        return runLog;
    }

    public void setRunLog(String runLog) {
        this.runLog = runLog;
    }

    public Target.Status getStatus() {
        return Target.Status.valueOf(status.toUpperCase());
    }

    public void setStatus(Target.Status status) {
        this.status = status.toString();
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
