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
import javafx.scene.layout.Pane;
import target.Target;
import target.TargetGraph;

public class GraphController {

    private final ObservableList<TargetTableItem> targetTableList = FXCollections.observableArrayList();
    private final ObservableList<TargetTypeSummery> typeSummeryList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetNameList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetInfoList = FXCollections.observableArrayList();
    private TargetGraph targetGraph;

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
    private Pane ImagePane;

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

    public void setTargetGraph(TargetGraph targetGraph)
    {
        this.targetGraph = targetGraph;
        initialize();
        setDependenciesTable();
        setTypeSummeryTable();
        setSerialSetComboBox();
        graphImageView.fitWidthProperty().bind(ImagePane.widthProperty());
        graphImageView.fitHeightProperty().bind(ImagePane.heightProperty());
        setGraphImageView();
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

    private void setGraphImageView()
    {
        Image image = generateGraphImage();
        if (image != null)
             graphImageView.setImage(image);
    }

    public Image generateGraphImage() {
        GraphViz graphViz = new GraphViz(targetGraph.getDirectory(),
                "yellow","blue","orange", "pink");
        graphViz.openGraph();
        for (Target target: targetGraph.getAllTargets().values()){
            graphViz.addNode(target);
            graphViz.addConnections(target,"black");
        }
        graphViz.closeGraph();
        return graphViz.generateImage();
    }
}

