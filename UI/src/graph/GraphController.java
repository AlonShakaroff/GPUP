package graph;

import graph.tableview.TargetTableItem;
import graph.tableview.TargetTypeSummery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import target.Target;
import target.TargetGraph;

public class GraphController {

    ObservableList<TargetTableItem> targetTableList = FXCollections.observableArrayList();
    ObservableList<TargetTypeSummery> typeSummeryList = FXCollections.observableArrayList();
    ObservableList<String> serialSetNameList = FXCollections.observableArrayList();
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
    private ListView<String> serialSetsListView;


    @FXML
    public void initialize() {
        initializeTargetTable();
        initializeTypeSummeryTable();
        initializeSerialSetListView();
    }

    public void initializeTargetTable() {
        name.setCellValueFactory(new PropertyValueFactory<TargetTableItem,String>("Name"));
        type.setCellValueFactory(new PropertyValueFactory<TargetTableItem,String>("Type"));
        dependsOnDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("Depends on directly"));
        dependsOnTotal.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("Depends on total"));
        requiredForDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("Required for directly"));
        requiredForTotal.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("Required for total"));
        serialSetsAmount.setCellValueFactory(new PropertyValueFactory<TargetTableItem,Integer>("Serial sets"));
    }

    public void initializeTypeSummeryTable() {
        targetsAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Targets"));
        rootAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Root"));
        middleAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Middle"));
        leafAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Leaf"));
        independentAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery,Integer>("Independents"));
    }

    public void initializeSerialSetListView() {

    }

    public void setTargetGraph(TargetGraph targetGraph)
    {
        this.targetGraph = targetGraph;
        setDependenciesTable();
        setTypeSummeryTable();
        setSerialSetListView();
    }

    private void setDependenciesTable() {
        TargetTableItem currentItem;

        for (Target target: targetGraph.getAllTargets().values()) {
            currentItem = new TargetTableItem(target);
            targetTableList.add(currentItem);
        }

        dependenciesTableView.setItems(targetTableList);
    }

    private void setTypeSummeryTable() {
        TargetTypeSummery typeSummeryItem = new TargetTypeSummery(targetGraph);
        typeSummeryList.add(typeSummeryItem);
        typeTableView.setItems(typeSummeryList);
    }

    private void setSerialSetListView() {

    }





}
