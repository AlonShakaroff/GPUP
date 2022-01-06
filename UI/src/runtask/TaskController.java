package runtask;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.bcel.internal.generic.NEWARRAY;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import target.TargetGraph;

import java.util.Set;

public class TaskController {

    TargetGraph targetGraph;
    ObservableList<String> TargetsNameList = FXCollections.observableArrayList();
    ObservableList<String> filteredTargetsNameList = FXCollections.observableArrayList();
    ObservableList<String> currentSelectedList = FXCollections.observableArrayList();
    ObservableList<String> currentSelectedInAddedTargetsList = FXCollections.observableArrayList();
    SimpleIntegerProperty howManyTargetsSelected;
    ListChangeListener<String> currentSelectedListListener;

    ObservableList<String> addedTargetsList = FXCollections.observableArrayList();

   public TaskController() {
        howManyTargetsSelected = new SimpleIntegerProperty(0);
        currentSelectedListListener = change -> howManyTargetsSelected.set(change.getList().size());
    }

    @FXML
    public void initialize() {
        requiredForButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        dependsOnButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        currentSelectedList = TargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedInAddedTargetsList = AddedTargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedList.addListener(currentSelectedListListener);
        addedTargetsList.clear();
    }
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
    private Color x22;

    @FXML
    private Font x12;

    @FXML
    private ListView<String> AddedTargetsListView;

    @FXML
    private Button selectAllButton;

    @FXML
    private Button deSelectAllButton;

    @FXML
    private Button allTargetsButton;

    @FXML
    private Button dependsOnButton;

    @FXML
    private Button requiredForButton;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private TableView<?> progressTableView;

    @FXML
    private TextArea runDetailsTextArea;

    @FXML
    private ProgressBar progressBar;

    @FXML
    void addButtonClicked(ActionEvent event) {
        addedTargetsList.addAll(currentSelectedList);
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void removeButtonClicked(ActionEvent event) {
        addedTargetsList.removeAll(currentSelectedInAddedTargetsList);
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void allTargetsButtonSelected(ActionEvent event) {
        TargetsListView.setItems(TargetsNameList);
    }

    @FXML
    void deSelectAllButtonClicked(ActionEvent event) {
        TargetsListView.getSelectionModel().clearSelection();
    }

    @FXML
    void dependsOnButtonClicked(ActionEvent event) {
        Set<String> dependsOnSet = targetGraph.getTarget(TargetsListView.getSelectionModel().getSelectedItem()).getAllDependsOnTargetsAsStrings();
        filteredTargetsNameList.clear();
        filteredTargetsNameList.addAll(dependsOnSet);
        TargetsListView.setItems(filteredTargetsNameList);
    }

    @FXML
    void requiredForButtonClicked(ActionEvent event) {
        Set<String> requiredForSet = targetGraph.getTarget(TargetsListView.getSelectionModel().getSelectedItem()).getAllRequiredForTargetsAsStrings();
        filteredTargetsNameList.clear();
        filteredTargetsNameList.addAll(requiredForSet);
        TargetsListView.setItems(filteredTargetsNameList);
    }

    @FXML
    void runTaskButtonClicked(ActionEvent event) {

    }

    @FXML
    void selectAllButtonClicked(ActionEvent event) {
        TargetsListView.getSelectionModel().selectAll();
    }

    @FXML
    void stopTaskButtonClicked(ActionEvent event) {

    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        setAllTargetsNameList();
    }

    private void setAllTargetsNameList() {
        TargetsNameList.clear();
        TargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        TargetsListView.setItems(TargetsNameList.sorted());
        TargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        AddedTargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }
}
