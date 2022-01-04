package graph.tableview;

import target.TargetGraph;

public class TargetTypeSummery {
    private int totalAmountOfTargets;
    private int independent;
    private int leaf;
    private int middle;
    private int root;

    public TargetTypeSummery(TargetGraph graph)
    {
        this.totalAmountOfTargets = graph.getAmountOfTargets();
        this.independent = graph.getAmountOfIndependent();
        this.leaf = graph.getAmountOfLeafs();
        this.middle = graph.getAmountOfMiddles();
        this.root = graph.getAmountOfRoots();
    }
}
