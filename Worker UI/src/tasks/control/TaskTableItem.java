package tasks.control;

public class TaskTableItem {
    private final String status;
    private final Integer amountOfWorkers;

    public TaskTableItem(String status, Integer amountOfWorkers){
        this.status = status;
        this.amountOfWorkers = amountOfWorkers;
    }

    public String getStatus() {
        return status;
    }

    public Integer getAmountOfWorkers() {
        return amountOfWorkers;
    }
}
