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

import java.io.*;

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

    public void setTargetGraph(TargetGraph targetGraph)
    {
        this.targetGraph = targetGraph;
        initialize();
        setDependenciesTable();
        setTypeSummeryTable();
        setSerialSetComboBox();
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
        byte[] img_stream = null;
        String directoryPath = targetGraph.getDirectory();
        String fileNameDOT = "GeneratedGraph.dot";
        String fileNamePNG = "GeneratedGraph.png";
        String createPNGFromDOT = "dot -Tpng "+ fileNameDOT + " -o " + fileNamePNG;
        String properties = "digraph G {\n" + "node [margin=0 fontcolor=black fontsize=28 width=2 shape=circle style=filled]\n" +
                "\n" +
                "nodesep = 2;\n" +
                "ranksep = 2;\n" + "penwidth = 5;\n";

        try {
            FileWriter dotFile = new FileWriter(new File(directoryPath,fileNameDOT));
            dotFile.write(properties);

            for (Target target : targetGraph.getAllTargets().values()) {
                dotFile.write(target.getName());
                if (!target.getDependsOnSet().isEmpty())
                    dotFile.write("-> {" + printAllDependsOnTarget(target) + "}\n");

                dotFile.write("\n");
            }
            dotFile.write("}");
            dotFile.close();

            Process process = Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd \\ && cd " + directoryPath + " && " + createPNGFromDOT + " && exit");
            process.waitFor();
            Thread.sleep(500);

            File img =  new File(targetGraph.getDirectory() + "/" + fileNamePNG);
            if (!img.exists())
                return null;
            FileInputStream in = new FileInputStream(img.getAbsolutePath());
            img_stream = new byte[in.available()];
            in.read(img_stream);
            // Close it if we need to
            if( in != null ) in.close();

            if (!img.delete())
                System.err.println("Warning: " + img.getAbsolutePath() + " could not be deleted!");
        }
        catch(InterruptedException | IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return new Image(new ByteArrayInputStream(img_stream));
    }

    private String printAllDependsOnTarget(Target curTarget)
    {
        String DependedTarget = "";
        for (Target dependsOnTarget : curTarget.getDependsOnSet())
        {
            DependedTarget = DependedTarget + dependsOnTarget.getName() + " ";
        }
        return DependedTarget;
    }

}

