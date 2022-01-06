package runtask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import target.TargetGraph;

public class TaskController {

    TargetGraph targetGraph;
    ObservableList<String> allTargetsNameList = FXCollections.observableArrayList();


    @FXML
    private Color x21;

    @FXML
    private Font x11;

    @FXML
    private TitledPane simulationTitledPane;

    @FXML
    private Spinner<Integer> simulationTimeSpinner;

    @FXML
    private CheckBox simulationRandomCheckBox;

    @FXML
    private Spinner<Double> simulationSuccessRateSpinner;

    @FXML
    private Spinner<Double> simulationWarningRateSpinner;

    @FXML
    private TitledPane compileTaskTitledPane;

    @FXML
    private TextField compileTaskSourceTextField;

    @FXML
    private Button compileTaskSourceSearchButton;

    @FXML
    private TextField compileTaskDestTextField;

    @FXML
    private Button compileTaskDestSearchButton;

    @FXML
    private CheckBox incrementalCheckBox;

    @FXML
    private Button runTaskButton;

    @FXML
    private Button pauseTaskButton;

    @FXML
    private Button stopTaskButton;

    @FXML
    private Color x2;

    @FXML
    private Font x1;

    @FXML
    private ListView<String> TargetsListView;

    @FXML
    private Button selectAllButton;

    @FXML
    private Button deSelectAllButton;

    @FXML
    private Button dependsOnButton;

    @FXML
    private Button reqForButton;

    @FXML
    private TableView<?> progressTableView;

    @FXML
    private TextArea runDetailsTextArea;

    @FXML
    private ProgressBar progressBar;

    @FXML
    void deSelectAllButtonClicked(ActionEvent event) {

    }

    @FXML
    void dependsOnButtonClicked(ActionEvent event) {

    }

    @FXML
    void reqForButtonClicked(ActionEvent event) {

    }

    @FXML
    void runTaskButtonClicked(ActionEvent event) {

    }

    @FXML
    void selectAllButtonClicked(ActionEvent event) {

    }

    @FXML
    void stopTaskButtonClicked(ActionEvent event) {

    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        setAllTargetsNameList();
    }

    private void setAllTargetsNameList() {
        allTargetsNameList.clear();
        allTargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        TargetsListView.setItems(allTargetsNameList.sorted());
        TargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
