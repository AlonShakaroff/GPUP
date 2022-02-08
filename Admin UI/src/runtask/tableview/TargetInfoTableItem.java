package runtask.tableview;

import target.Target;

import java.util.List;
import java.util.Set;

public class TargetInfoTableItem {
    private String name;
    private String type;
    private Set<String> serialSets;
    private String status;

    public TargetInfoTableItem(Target target) {
        this.name = target.getName();
        this.type = target.getNodeTypeAsString();
        this.status = target.getRunStatusAsString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getSerialSets() {
        return serialSets;
    }

    public void setSerialSets(Set<String> serialSets) {
        this.serialSets = serialSets;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
