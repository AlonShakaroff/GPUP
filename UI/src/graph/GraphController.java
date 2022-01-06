package graph;

import graph.tableview.TargetTableItem;
import graph.tableview.TargetTypeSummery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import target.Target;
import target.TargetGraph;

import java.io.File;

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
    private ComboBox<String> serialSetComboBox;

    @FXML
    private ListView<String> serialSetsListView;

    @FXML
    private ImageView graphImageView;


    @FXML
    public void initialize() {
        initializeTargetTable();
        initializeTypeSummeryTable();
        initializeSerialSetComboBox();
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

    public void initializeSerialSetComboBox() {
        serialSetComboBox.setOnAction((event) -> {
            serialSetInfoList.clear();
            if(!targetGraph.getSerialSets().isEmpty()) {
                serialSetInfoList.addAll(targetGraph.getSerialSets().get(serialSetComboBox.getValue()));
                serialSetsListView.setItems(serialSetInfoList.sorted());
            }
        });
    }

    public void initializeGraphImage() {
//        graphImageView.setImage();
    }

    public void setTargetGraph(TargetGraph targetGraph)
    {
        this.targetGraph = targetGraph;
        initialize();
        setDependenciesTable();
        setTypeSummeryTable();
        setSerialSetComboBox();

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

    private void setSerialSetComboBox() {
        serialSetNameList.clear();
        serialSetInfoList.clear();
        if(targetGraph.getSerialSets() != null) {
            serialSetComboBox.setDisable(false);
            serialSetsListView.setDisable(false);
            serialSetNameList.addAll(targetGraph.getSerialSets().keySet());
            serialSetComboBox.setItems(serialSetNameList.sorted());
        }
        else {
            serialSetComboBox.setDisable(true);
            serialSetInfoList.add("No serial sets");
            serialSetsListView.setItems(serialSetInfoList);
            serialSetsListView.setDisable(true);
        }
        serialSetComboBox.setTooltip
                (new Tooltip("Choose a serial set to display all the targets that belong to it"));
    }

    //--------------------------------------------graphviz-----------------------------------------------------

    public void graphToImage(String type)
    {
        GraphViz gv=new GraphViz();
        gv.addln(gv.start_graph());
        for (Target target: targetGraph.getAllTargets().values()) {
            gv.add(target.getName());
            if (!target.getDependsOnSet().isEmpty()) {
                gv.add(gv.start_subgraph());
                for (Target dependTarget : target.getDependsOnSet()) {
                    gv.add(dependTarget.getName() + " ");
                }
                gv.add(gv.end_subgraph());
            }
            gv.addln();
        }
        gv.addln(gv.end_graph());
        gv.increaseDpi();
        File out = new File("graph."+ type);
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
    }

//    private void setGraphImageView() {
//        graphToImage("png");
//        graphImageView.setImage(getClass().getResource());
//    }

}

