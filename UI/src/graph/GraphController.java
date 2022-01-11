package graph;

import graph.tableview.TargetTableItem;
import graph.tableview.TargetTypeSummery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import target.Target;
import target.TargetGraph;

import java.io.File;

public class GraphController {

    private static final int CHAR_WIDTH=8;
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
    private TableColumn<TargetTableItem, String> extraData;

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
    private Button saveGraphButton;

    @FXML
    private Pane ImagePane;

    @FXML
    public void initialize() {
        initializeTargetTable();
        initializeTypeSummeryTable();
        initializeSerialSetComboBox();
    }

    public void initializeTargetTable() {
        name.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("Name"));
        type.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("Type"));
        dependsOnDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("DependsOnDirectly"));
        dependsOnTotal.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("DependsOnTotal"));
        requiredForDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("RequiredForDirectly"));
        requiredForTotal.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("RequiredForTotal"));
        serialSetsAmount.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("AmountOfSerialSets"));
        extraData.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("ExtraData"));
    }

    public void initializeTypeSummeryTable() {
        targetsAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("TotalAmountOfTargets"));
        rootAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Root"));
        middleAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Middle"));
        leafAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Leaf"));
        independentAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Independent"));
    }

    public void initializeSerialSetComboBox() {
        serialSetComboBox.setOnAction((event) -> {
            serialSetInfoList.clear();
            if (!targetGraph.getSerialSets().isEmpty()) {
                serialSetInfoList.addAll(targetGraph.getSerialSets().get(serialSetComboBox.getValue()));
                serialSetsListView.setItems(serialSetInfoList.sorted());
            }
        });
    }

    public void setTargetGraph(TargetGraph targetGraph) {
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
        int prefWidth = 0;
        targetTableList.clear();
        for (Target target : targetGraph.getAllTargets().values()) {
            prefWidth = Math.max(prefWidth,CHAR_WIDTH*target.getExtraData().length());
            currentItem = new TargetTableItem(target);
            targetTableList.add(currentItem);
        }

        extraData.setPrefWidth(prefWidth);
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
        if (targetGraph.getSerialSets() != null) {
            serialSetComboBox.setDisable(false);
            serialSetsListView.setDisable(false);
            serialSetNameList.addAll(targetGraph.getSerialSets().keySet());
            serialSetComboBox.setItems(serialSetNameList.sorted());
        } else {
            serialSetComboBox.setDisable(true);
            serialSetInfoList.add("No serial sets");
            serialSetsListView.setItems(serialSetInfoList);
            serialSetsListView.setDisable(true);
        }
        serialSetComboBox.setTooltip
                (new Tooltip("Choose a serial set to display all the targets that belong to it"));
    }

    //--------------------------------------------graphviz-----------------------------------------------------

    private void setGraphImageView() {
        Image image = generateGraphImage();
        if (image != null)
            graphImageView.setImage(image);
    }

    public Image generateGraphImage() {
        GraphViz graphViz = new GraphViz(targetGraph.getDirectory(),
                "yellow", "blue", "orange", "pink");
        graphViz.openGraph();
        for (Target target : targetGraph.getAllTargets().values()) {
            graphViz.addNode(target);
            graphViz.addConnections(target, "black");
        }
        graphViz.closeGraph();
        return graphViz.generateImage();
    }


    @FXML
    void saveGraphButtonClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png file",".png"));
        File chosenFile = fileChooser.showSaveDialog(saveGraphButton.getScene().getWindow());
        if (chosenFile != null) {
            GraphViz graphViz = new GraphViz(chosenFile.getParent(),
                    "yellow", "blue", "orange", "pink");
            graphViz.openGraph();
            for (Target target : targetGraph.getAllTargets().values()) {
                graphViz.addNode(target);
                graphViz.addConnections(target, "black");
            }
            graphViz.closeGraph();
            String name = chosenFile.getName();

            graphViz.saveImage(name.substring(0, name.lastIndexOf(".")));
        }
    }
}

