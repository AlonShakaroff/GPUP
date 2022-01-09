package runtask;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import runtask.tableview.TargetInfoTableItem;
import target.TargetGraph;
import task.SimulationTask;
import task.Task;

import java.io.File;
import java.util.Set;

public class TaskController {

    private TargetGraph targetGraph;
    private Task currentTask;
    private String lastVisitedDirectory = System.getProperty("user.home");
    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    private final SimpleIntegerProperty howManyTargetsSelected;
    private final SimpleIntegerProperty howManyTargetsAdded;
    private final SimpleBooleanProperty isATargetSelected;

    private final ListChangeListener<String> currentSelectedListListener;
    private final ListChangeListener<String> currentAddedListListener;
    private final ListChangeListener<String> currentSelectedFrozenListener;
    private final ListChangeListener<String> currentSelectedSkippedListener;
    private final ListChangeListener<String> currentSelectedWaitingListener;
    private final ListChangeListener<String> currentSelectedInProcessListener;
    private final ListChangeListener<String> currentSelectedFinishedListener;



    private final ObservableList<String> TargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> filteredTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> addedTargetsList = FXCollections.observableArrayList();
    private final ObservableList<String> frozenTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> skippedTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> waitingTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> inProcessTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> finishedTargetsNameList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedInAddedTargetsList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedFrozenList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedSkippedList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedWaitingList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedInProcessList = FXCollections.observableArrayList();
    private  ObservableList<String> currentSelectedFinishedList = FXCollections.observableArrayList();

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
       isATargetSelected = new SimpleBooleanProperty(false);
       currentSelectedFrozenListener = change -> {
           if(!change.getList().isEmpty()) {
               isATargetSelected.set(true);
               currentSelectedSkippedList.clear();
               currentSelectedWaitingList.clear();
               currentSelectedInProcessList.clear();
               currentSelectedFinishedList.clear();
           }
       };
       currentSelectedSkippedListener = change -> {
           if(!change.getList().isEmpty()) {
               isATargetSelected.set(true);
               currentSelectedFrozenList.clear();
               currentSelectedWaitingList.clear();
               currentSelectedInProcessList.clear();
               currentSelectedFinishedList.clear();
           }
       };
       currentSelectedWaitingListener = change -> {
           if(!change.getList().isEmpty()) {
               isATargetSelected.set(true);
               currentSelectedSkippedList.clear();
               currentSelectedFrozenList.clear();
               currentSelectedInProcessList.clear();
               currentSelectedFinishedList.clear();
           }
       };
       currentSelectedInProcessListener = change -> {
           if(!change.getList().isEmpty()) {
               isATargetSelected.set(true);
               currentSelectedSkippedList.clear();
               currentSelectedWaitingList.clear();
               currentSelectedFrozenList.clear();
               currentSelectedFinishedList.clear();
           }
       };
       currentSelectedFinishedListener = change -> {
           if(!change.getList().isEmpty()) {
               isATargetSelected.set(true);
               currentSelectedSkippedList.clear();
               currentSelectedWaitingList.clear();
               currentSelectedInProcessList.clear();
               currentSelectedFrozenList.clear();
           }
       };
    }

    @FXML
    public void initialize() {
        initializeTargetInfoTable();

        requiredForButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        dependsOnButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        currentSelectedList = TargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedInAddedTargetsList = AddedTargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedFrozenList = FrozenListView.getSelectionModel().getSelectedItems();
        currentSelectedSkippedList = SkippedListView.getSelectionModel().getSelectedItems();
        currentSelectedWaitingList = WaitingListView.getSelectionModel().getSelectedItems();
        currentSelectedInProcessList = InProcessListView.getSelectionModel().getSelectedItems();
        currentSelectedFinishedList = FinishedListView.getSelectionModel().getSelectedItems();
        currentSelectedList.addListener(currentSelectedListListener);
        addedTargetsList.addListener(currentAddedListListener);
        currentSelectedFrozenList.addListener(currentSelectedFrozenListener);
        currentSelectedSkippedList.addListener(currentSelectedSkippedListener);
        currentSelectedWaitingList.addListener(currentSelectedWaitingListener);
        currentSelectedInProcessList.addListener(currentSelectedInProcessListener);
        currentSelectedFinishedList.addListener(currentSelectedFinishedListener);
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
    private TextArea runDetailsTextArea;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Spinner<Integer> ParallelismSpinner;

    @FXML
    private ListView<String> FrozenListView;

    @FXML
    private ListView<String> SkippedListView;

    @FXML
    private ListView<String> WaitingListView;

    @FXML
    private ListView<String> InProcessListView;

    @FXML
    private ListView<String> FinishedListView;

    @FXML
    private TableView<TargetInfoTableItem> TargetInfoTableView;

    @FXML
    private TableColumn<TargetInfoTableItem, String> name;

    @FXML
    private TableColumn<TargetInfoTableItem, String> type;

    @FXML
    private TableColumn<TargetInfoTableItem, Set<String>> serialSets;

    @FXML
    private TableColumn<TargetInfoTableItem, String> status;

    @FXML
    private TextArea TargetInfoTextArea;


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
        targetGraph.markTargetsAsChosen(addedTargetsList);
        if(simulationTitledPane.isExpanded())
            currentTask = new SimulationTask(simulationTimeSpinner.getValue(),
                    simulationRandomCheckBox.isSelected(), simulationSuccessRateSpinner.getValue(),WarningRateValueFactory.getValue());
        currentTask.runTaskOnGraph(targetGraph);
    }

    @FXML
    void selectAllButtonClicked(ActionEvent event) {
        TargetsListView.getSelectionModel().selectAll();
    }

    @FXML
    void stopTaskButtonClicked(ActionEvent event) {

    }

    @FXML
    void parallelismTextInputEntered(InputMethodEvent event) {
        System.out.println(event.getCommitted());
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

    public void initializeTargetInfoTable() {
        name.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem,String>("Name"));
        type.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem,String>("Type"));
        serialSets.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem,Set<String>>("SerialSets"));
        status.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem,String>("Status"));
    }
}
