package graph.tableview;

import target.Target;

public class TargetTableItem {
    private String name;
    private String type;
    private Integer dependsOnDirectly;
    private Integer dependsOnTotal;
    private Integer requiredForDirectly;
    private Integer requiredForTotal;
    private Integer amountOfSerialSets;

    public TargetTableItem(Target target)
    {
        this.name = target.getName();
        this.type = target.getNodeTypeAsString();
        this.dependsOnDirectly = target.getAmountOfDirectlyDependsOn();
        this.dependsOnTotal = target.getAmountOfTotalDependsOn();
        this.requiredForDirectly = target.getAmountOfDirectlyRequiredFor();
        this.requiredForTotal = target.getAmountOfTotalRequiredFor();
        this.amountOfSerialSets = target.getAmountOfSerialSets();
    }
}
