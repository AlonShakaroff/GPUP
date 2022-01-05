package graph;

import graph.tableview.TargetTableItem;
import graph.tableview.TargetTypeSummery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import target.Target;
import target.TargetGraph;

public class GraphController {

    ObservableList<TargetTableItem> targetTableList = FXCollections.observableArrayList();
    ObservableList<TargetTypeSummery> typeSummeryList = FXCollections.observableArrayList();
    ObservableList<String> serialSetNameList = FXCollections.observableArrayList();
    ObservableList<String> serialSetInfoList = FXCollections.observableArrayList();
    TargetGraph targetGraph;

    @FXML
    private TableView<TargetTableItem> dependenciesTableView;

    @FXML
    private TableColumn<TargetTableItem, String> name;

    @FXML
    private TableColumn<TargetTableItem, String> type;

    @FXML
    private TableColumn<TargetTableItem, Integer> dependsOnDirectly;

    @FXML
    private TableColumn<TargetTableItem, Integer> dependsOnTotal;

    @FXML
    private TableColumn<TargetTableItem, Integer> requiredForDirectly;

    @FXML
    private TableColumn<TargetTableItem, Integer> requiredForTotal;

    @FXML
    private TableColumn<TargetTableItem, Integer> serialSetsAmount;

    @FXML
    private TableView<TargetTypeSummery> typeTableView;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> targetsAmount;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> independentAmount;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> leafAmount;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> middleAmount;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> rootAmount;

    @FXML
    private ChoiceBox<String> serialSetChoiceBox;

    @FXML
    private ListView<String> serialSetsListView;


    @FXML
    public void initialize() {
        initializeTargetTable();
        initializeTypeSummeryTable();
        initializeSerialSetChoiceBox();
    }

    public void initializeTargetTable() {
        name.setCellValueFactory(new PropertyValueFactory<TargetTableItem,String>("Name"));
        type.setCellValueFactory(new PropertyValueFactory<TargetTableItem,String>("Type"));
        dependsOnDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("DependsOnDirectly"));
        dependsOnTotal.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("DependsOnTotal"));
        requiredForDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("RequiredForDirectly"));
        requiredForTotal.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("RequiredForTotal"));
        serialSetsAmount.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("AmountOfSerialSets"));
    }

    public void initializeTypeSummeryTable() {
        targetsAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("TotalAmountOfTargets"));
        rootAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Root"));
        middleAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Middle"));
        leafAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Leaf"));
        independentAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Independent"));
    }

    public void initializeSerialSetChoiceBox() {
        serialSetChoiceBox.setOnAction((event) -> {
            serialSetInfoList.clear();
            if(!targetGraph.getSerialSets().isEmpty()) {
                serialSetInfoList.addAll(targetGraph.getSerialSets().get(serialSetChoiceBox.getValue()));
                serialSetsListView.setItems(serialSetInfoList.sorted());
            }
        });
    }

    public void setTargetGraph(TargetGraph targetGraph)
    {
        this.targetGraph = targetGraph;
        setDependenciesTable();
        setTypeSummeryTable();
        setSerialSetChoiceBox();
    }

    private void setDependenciesTable() {
        TargetTableItem currentItem;
        targetTableList.clear();
        for (Target target: targetGraph.getAllTargets().values()) {
            currentItem = new TargetTableItem(target);
            targetTableList.add(currentItem);
        }

        dependenciesTableView.setItems(targetTableList);
    }

    private void setTypeSummeryTable() {
        typeSummeryList.clear();
        TargetTypeSummery typeSummeryItem = new TargetTypeSummery(targetGraph);
        typeSummeryList.add(typeSummeryItem);
        typeTableView.setItems(typeSummeryList);
    }

    private void setSerialSetChoiceBox() {
        serialSetNameList.clear();

        if(!targetGraph.getSerialSets().isEmpty()) {
            serialSetNameList.addAll(targetGraph.getSerialSets().keySet());
            serialSetChoiceBox.setItems(serialSetNameList.sorted());
        }

        serialSetChoiceBox.setTooltip
                (new Tooltip("Choose a serial set to display all the targets that belong to it"));
    }

}
