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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import target.Target;
import target.TargetGraph;

import javax.xml.ws.Holder;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GraphController {

    private static final int CHAR_WIDTH=8;
    private final ObservableList<TargetTableItem> targetTableList = FXCollections.observableArrayList();
    private final ObservableList<TargetTypeSummery> typeSummeryList = FXCollections.observableArrayList();
    private TargetGraph targetGraph;
    private TreeItem<String> BigRootRoots;
    private TreeItem<String> BigRootLeaves;

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
    private ImageView graphImageView;

    @FXML
    private Button saveGraphButton;

    @FXML
    private Pane ImagePane;

    @FXML
    private TreeView<String> treeView;

    @FXML
    private RadioButton rootsRadioButton;

    @FXML
    private ToggleGroup View;

    @FXML
    private RadioButton LeavesRadioButton;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField TypeTextField;

    @FXML
    private TextField requierdTextField;

    @FXML
    private TextField dependsTextField;

    @FXML
    void selectItem(MouseEvent event) {
        TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();

        if (item != null) {
            try {
                Target target = targetGraph.getAllTargets().get(item.getValue().split("  -  ")[0]);
                nameTextField.setText(target.getName());
                TypeTextField.setText(target.getNodeTypeAsString());
                dependsTextField.setText(target.getAllDependsOnTargets().toString());
                requierdTextField.setText(target.getAllRequiredForTargets().toString());
            }
            catch (Exception ignore){}
        }
    }

    @FXML
    public void initialize() {
        initializeTargetTable();
        initializeTypeSummeryTable();
        rootsRadioButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.equals(true))
                treeView.setRoot(BigRootRoots);
            else
                treeView.setRoot(BigRootLeaves);}));
    }


    public void initializeTargetTable() {
        name.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("Name"));
        type.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("Type"));
        dependsOnDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("DependsOnDirectly"));
        dependsOnTotal.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("DependsOnTotal"));
        requiredForDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("RequiredForDirectly"));
        requiredForTotal.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("RequiredForTotal"));
        extraData.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("ExtraData"));
    }

    public void initializeTypeSummeryTable() {
        targetsAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("TotalAmountOfTargets"));
        rootAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Root"));
        middleAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Middle"));
        leafAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Leaf"));
        independentAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Independent"));
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        initialize();
        setDependenciesTable();
        setTypeSummeryTable();
        treeViewRoots();
        treeViewLeaves();
        treeView.setShowRoot(false);
        treeView.setRoot(BigRootRoots);
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


    private void treeViewRoots(){
        BigRootRoots = new TreeItem<>("root");
        Set<Target> addedTargets = new HashSet<>();
        targetGraph.getAllTargets().values().stream().filter(target -> (target.getNodeType().equals(Target.Type.ROOT)
        || target.getNodeType().equals(Target.Type.INDEPENDENT))).forEach(target -> {
            TreeItem<String> RootItem = new TreeItem<>(target.getName() + "  -  " + target.getNodeTypeAsString());
            addedTargets.add(target);
            addDependsTree(target, RootItem, addedTargets);
            addedTargets.remove(target);
            BigRootRoots.getChildren().add(RootItem);
        });
    }
    private void addDependsTree(Target target, TreeItem<String> RootItem, Set<Target> addedTargets){
        if (target.getDependsOnSet().isEmpty())
            return;
        target.getDependsOnSet().forEach(target1 -> {
            if (!addedTargets.contains(target1)) {
                TreeItem<String> MidItem = new TreeItem<>(target1.getName() + "  -  " + target1.getNodeTypeAsString());
                addedTargets.add(target1);
                addDependsTree(target1, MidItem, addedTargets);
                addedTargets.remove(target1);
                RootItem.getChildren().add(MidItem);
            }
        });
    }

    private void treeViewLeaves(){
        BigRootLeaves = new TreeItem<>("root");
        Set<Target> addedTargets = new HashSet<>();
        targetGraph.getAllTargets().values().stream().filter(target -> (target.getNodeType().equals(Target.Type.LEAF)
                || target.getNodeType().equals(Target.Type.INDEPENDENT))).forEach(target -> {
            TreeItem<String> RootItem = new TreeItem<>(target.getName() + "  -  " + target.getNodeTypeAsString());
            addedTargets.add(target);
            addRequiredTree(target, RootItem,addedTargets);
            addedTargets.remove(target);
            BigRootLeaves.getChildren().add(RootItem);
        });
    }

    private void addRequiredTree(Target target, TreeItem<String> RootItem, Set<Target> addedTargets){
        if (target.getRequiredForSet().isEmpty())
            return;
        target.getRequiredForSet().forEach(target1 -> {
            if (!addedTargets.contains(target1)) {
                TreeItem<String> MidItem = new TreeItem<>(target1.getName() + "  -  " + target1.getNodeTypeAsString());
                addedTargets.add(target1);
                addRequiredTree(target1, MidItem, addedTargets);
                addedTargets.remove(target1);
                RootItem.getChildren().add(MidItem);
            }
        });
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

