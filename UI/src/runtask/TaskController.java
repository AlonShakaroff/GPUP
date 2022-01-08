package runtask;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.bcel.internal.generic.NEWARRAY;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import target.TargetGraph;

import java.awt.*;
import java.io.File;
import java.util.Optional;
import java.util.Set;

public class TaskController {

    private TargetGraph targetGraph;
    private String lastVisitedDirectory = System.getProperty("user.home");
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final ObservableList<String> TargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> filteredTargetsNameList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedInAddedTargetsList = FXCollections.observableArrayList();
    private final ObservableList<String> addedTargetsList = FXCollections.observableArrayList();
    private final SimpleIntegerProperty howManyTargetsSelected;
    private final SimpleIntegerProperty howManyTargetsAdded;
    private final ListChangeListener<String> currentSelectedListListener;
    private final ListChangeListener<String> currentAddedListListener;

    private final SpinnerValueFactory<Double> successRateValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0 , 1.0, 0.50, 0.01);
    private final SpinnerValueFactory<Double> WarningRateValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0 , 1.0, 0.50, 0.01);
    private final SpinnerValueFactory<Integer> TimeValueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0,Integer.MAX_VALUE, 1000);
    private SpinnerValueFactory<Integer> ParallelValueFactory;

   public TaskController() {
        howManyTargetsSelected = new SimpleIntegerProperty(0);
        currentSelectedListListener = change -> howManyTargetsSelected.set(change.getList().size());
        howManyTargetsAdded = new SimpleIntegerProperty(0);
        currentAddedListListener = change -> howManyTargetsAdded.set(change.getList().size());
    }

    @FXML
    public void initialize() {
        requiredForButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        dependsOnButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        currentSelectedList = TargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedInAddedTargetsList = AddedTargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedList.addListener(currentSelectedListListener);
        addedTargetsList.addListener(currentAddedListListener);
        addedTargetsList.clear();

        runTaskButton.disableProperty().bind(Bindings.and(howManyTargetsAdded.isNotEqualTo(0),
                Bindings.or(simulationTitledPane.expandedProperty(),
                        Bindings.and(compileTaskTitledPane.expandedProperty(),
                                Bindings.and(compileTaskSourceTextField.textProperty().isNotEqualTo(""),
                                        compileTaskDestTextField.textProperty().isNotEqualTo(""))))).not());

        simulationSuccessRateSpinner.setValueFactory(successRateValueFactory);
        simulationWarningRateSpinner.setValueFactory(WarningRateValueFactory);
        simulationTimeSpinner.setValueFactory(TimeValueFactory);
        simulationSuccessRateSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                simulationSuccessRateSpinner.getValueFactory().setValue(0.0);
        });
        simulationWarningRateSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                simulationWarningRateSpinner.getValueFactory().setValue(0.0);
        });
        simulationTimeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                simulationTimeSpinner.getValueFactory().setValue(0);
        });
        ParallelismSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                ParallelismSpinner.getValueFactory().setValue(1);
        });
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
    private Button clearButton;

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
    private Spinner<Integer> ParallelismSpinner;

    @FXML
    private ListView<String> TargetInfoListView;

    @FXML
    void addButtonClicked(ActionEvent event) {
        for(String str: currentSelectedList)
            if(!addedTargetsList.contains(str))
                addedTargetsList.add(str);
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void clearButtonClicked(ActionEvent event) {
        addedTargetsList.clear();
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
        ParallelValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,targetGraph.getMaxParallelism(), 1);
        ParallelismSpinner.setValueFactory(ParallelValueFactory);
    }

    private void setAllTargetsNameList() {
        TargetsNameList.clear();
        TargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        TargetsListView.setItems(TargetsNameList.sorted());
        TargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        AddedTargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    @FXML
    void compileTaskDestSearchButtonClicked(ActionEvent event) {
        directoryChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File dir = directoryChooser.showDialog(compileTaskDestSearchButton.getScene().getWindow());
        if (dir != null) {
            lastVisitedDirectory = dir.getPath();
            compileTaskDestTextField.textProperty().setValue(dir.getPath());
        }
    }

    @FXML
    void compileTaskSourceSearchButtonClicked(ActionEvent event) {
        directoryChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File dir = directoryChooser.showDialog(compileTaskSourceSearchButton.getScene().getWindow());
        if (dir != null) {
            lastVisitedDirectory = dir.getPath();
            compileTaskSourceTextField.textProperty().setValue(dir.getPath());
        }
    }
}
