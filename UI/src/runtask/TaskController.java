package runtask;

import javafx.beans.InvalidationListener;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import runtask.tableview.TargetInfoTableItem;
import target.Target;
import target.TargetGraph;
import task.GPUPTask;
import task.ExecutorThread;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskController {

    private TargetGraph targetGraph;
    private GPUPTask currentGPUPTask;
    private String lastVisitedDirectory = System.getProperty("user.home");
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private ExecutorThread taskThread;
    private Thread dataRefreshThread;
    private Task<Void> task;
    private String lastTask = "";

    private SimpleBooleanProperty isPaused;
    private final SimpleIntegerProperty howManyTargetsSelected;
    private final SimpleIntegerProperty howManyTargetsAdded;
    private final SimpleBooleanProperty isATargetSelected;
    private final SimpleBooleanProperty isIncrementalPossible;

    private final ChangeListener<Boolean> isPausedListener;
    private final ListChangeListener<String> currentSelectedListListener;
    private final ListChangeListener<String> currentAddedListListener;
    private final ListChangeListener<String> currentSelectedFrozenListener;
    private final ListChangeListener<String> currentSelectedSkippedListener;
    private final ListChangeListener<String> currentSelectedWaitingListener;
    private final ListChangeListener<String> currentSelectedInProcessListener;
    private final ListChangeListener<String> currentSelectedFinishedListener;
    private final InvalidationListener incrementalCheckboxInvalidListener;


    private final ObservableList<String> TargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> filteredTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> addedTargetsList = FXCollections.observableArrayList();
    private final ObservableList<String> frozenTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> skippedTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> waitingTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> inProcessTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> finishedTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<TargetInfoTableItem> targetInfoTableList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedInAddedTargetsList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedFrozenList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedSkippedList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedWaitingList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedInProcessList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedFinishedList = FXCollections.observableArrayList();


    private final SpinnerValueFactory<Double> successRateValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.50, 0.01);
    private final SpinnerValueFactory<Double> WarningRateValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.50, 0.01);
    private final SpinnerValueFactory<Integer> TimeValueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 5000);
    private SpinnerValueFactory<Integer> ParallelValueFactory;

   public TaskController() {
       isPaused = new SimpleBooleanProperty(false);
       howManyTargetsSelected = new SimpleIntegerProperty(0);
       currentSelectedListListener = change -> howManyTargetsSelected.set(change.getList().size());
       howManyTargetsAdded = new SimpleIntegerProperty(0);
       isATargetSelected = new SimpleBooleanProperty(false);
       isIncrementalPossible = new SimpleBooleanProperty(false);
       currentAddedListListener = change -> {
           howManyTargetsAdded.set(change.getList().size());
           isIncrementalPossible.set(true);
           if (change.getList().isEmpty()) {
               isIncrementalPossible.set(false);
           } else {
               if (targetGraph.getAllTargets().values().stream().filter(Target::isChosen).count() != change.getList().size())
                   isIncrementalPossible.set(false);
               else for (String targetName : change.getList()) {
                   if (!targetGraph.getAllTargets().get(targetName).isChosen())
                       isIncrementalPossible.set(false);
               }
               if (targetGraph.getAllTargets().values().stream().filter(Target::isChosen).allMatch
                       (target -> (target.getRunResult() == Target.Result.SUCCESS ||
                               target.getRunResult() == Target.Result.WARNING))) {
                   isIncrementalPossible.set(false);
               }
           }
       };
       currentSelectedFrozenListener = change -> {
           if (!change.getList().isEmpty()) {
               updateTargetDetailsTableAndTextArea(change.getList().get(0));
               isATargetSelected.set(true);
               SkippedListView.getSelectionModel().clearSelection();
               WaitingListView.getSelectionModel().clearSelection();
               InProcessListView.getSelectionModel().clearSelection();
               FinishedListView.getSelectionModel().clearSelection();
           }
       };
       currentSelectedSkippedListener = change -> {
           if (!change.getList().isEmpty()) {
               updateTargetDetailsTableAndTextArea(change.getList().get(0));
               isATargetSelected.set(true);
               FrozenListView.getSelectionModel().clearSelection();
               WaitingListView.getSelectionModel().clearSelection();
               InProcessListView.getSelectionModel().clearSelection();
               FinishedListView.getSelectionModel().clearSelection();
           }
       };
       currentSelectedWaitingListener = change -> {
           if (!change.getList().isEmpty()) {
               updateTargetDetailsTableAndTextArea(change.getList().get(0));
               isATargetSelected.set(true);
               FrozenListView.getSelectionModel().clearSelection();
               SkippedListView.getSelectionModel().clearSelection();
               InProcessListView.getSelectionModel().clearSelection();
               FinishedListView.getSelectionModel().clearSelection();
           }
       };
       currentSelectedInProcessListener = change -> {
           if (!change.getList().isEmpty()) {
               updateTargetDetailsTableAndTextArea(change.getList().get(0));
               isATargetSelected.set(true);
               FrozenListView.getSelectionModel().clearSelection();
               SkippedListView.getSelectionModel().clearSelection();
               WaitingListView.getSelectionModel().clearSelection();
               FinishedListView.getSelectionModel().clearSelection();
           }
       };
       currentSelectedFinishedListener = change -> {
           if (!change.getList().isEmpty()) {
               updateTargetDetailsTableAndTextArea(change.getList().get(0));
               isATargetSelected.set(true);
               FrozenListView.getSelectionModel().clearSelection();
               SkippedListView.getSelectionModel().clearSelection();
               WaitingListView.getSelectionModel().clearSelection();
               InProcessListView.getSelectionModel().clearSelection();
           }
       };
       isPausedListener = (observable, oldValue, newValue) -> {
           if (newValue)
               pauseTaskButton.setText("resume");
           else
               pauseTaskButton.setText("pause");
       };
       isPaused.addListener(isPausedListener);

       incrementalCheckboxInvalidListener = change -> {
           incrementalCheckBox.setSelected(false);
       };

   }

    private void updateTargetDetailsTableAndTextArea(String selectedTargetString) {
        Target selectedTarget = targetGraph.getTarget(selectedTargetString.split(" ")[0]);
        targetInfoTableList.clear();
        targetInfoTableList.add(new TargetInfoTableItem(selectedTarget));
        TargetInfoTableView.setItems(targetInfoTableList);

        TargetInfoTextArea.clear();
        statusUniqueDataDisplay(selectedTarget, selectedTarget.getRunStatus());
    }

    private void statusUniqueDataDisplay(Target selectedTarget, Target.Status status) {
        String uniqueData = "";
        switch(status) {
            case FROZEN:
                uniqueData = "Waiting for targets: \n" + selectedTarget.getDependsOnSet().stream()
                        .filter(Target::isChosen)
                            .filter(target ->  (target.getRunStatus().equals(Target.Status.FROZEN) ||
                                    target.getRunStatus().equals(Target.Status.WAITING) || target.getRunStatus().equals(Target.Status.IN_PROCESS)))
                                        .collect(Collectors.toList()).toString() + "\nto finish running successfully.";

                break;
            case SKIPPED:
                if (selectedTarget.getResponsibleTargets().isEmpty())
                    uniqueData = "Target was Interrupted!\n";
                else
                    uniqueData = "Skipped because targets:\n" + selectedTarget.getResponsibleTargets().toString() + "\nfailed.";
                break;
            case WAITING:
                uniqueData = "Target is waiting for: " + selectedTarget.getTimeInState() + " MS";
                break;
            case IN_PROCESS:
                uniqueData = "Target is in process for: " + selectedTarget.getTimeInState() + " MS";
                break;
            case FINISHED:
                if(selectedTarget.getRunResult().equals(Target.Result.SUCCESS))
                    uniqueData = "Target finished running successfully.";
                else if (selectedTarget.getRunResult().equals(Target.Result.WARNING))
                    uniqueData = "Target finished running successfully\nwith warning.";
                else
                    uniqueData = "Target FAILED.";
                break;
        }
        TargetInfoTextArea.appendText(uniqueData);
    }

    @FXML
    public void initialize() {
        initializeTargetInfoTable();

        TargetsListView.disableProperty().bind(pauseTaskButton.disableProperty().not());
        AddedTargetsListView.disableProperty().bind(pauseTaskButton.disableProperty().not());
        incrementalCheckBox.disableProperty().bind(isIncrementalPossible.not());
        incrementalCheckBox.disableProperty().addListener(incrementalCheckboxInvalidListener);
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
        compileTaskTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && lastTask.equals("Simulation"))
                isIncrementalPossible.set(false);
        });
        simulationTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && lastTask.equals("Compilation"))
                isIncrementalPossible.set(false);
        });
        addedTargetsList.clear();

        runTaskButton.disableProperty().bind(Bindings.and(stopTaskButton.disableProperty(),
                Bindings.and(pauseTaskButton.disableProperty()
                        , Bindings.and(howManyTargetsAdded.isNotEqualTo(0),
                                Bindings.or(simulationTitledPane.expandedProperty(),
                                        Bindings.and(compileTaskTitledPane.expandedProperty(),
                                                Bindings.and(compileTaskSourceTextField.textProperty().isNotEqualTo(""),
                                                        compileTaskDestTextField.textProperty().isNotEqualTo(""))))))).not());

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

        TargetsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2)
                    addButton.fire();
            }
        });
        AddedTargetsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2)
                    removeButton.fire();
            }
        });

        FrozenListView.setItems(frozenTargetsNameList);
        SkippedListView.setItems(skippedTargetsNameList);
        WaitingListView.setItems(waitingTargetsNameList);
        InProcessListView.setItems(inProcessTargetsNameList);
        FinishedListView.setItems(finishedTargetsNameList);

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
    private Label progressBarLabel;

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
        for (String str : currentSelectedList)
            if (!addedTargetsList.contains(str))
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
        resetDataLists();
        runDetailsTextArea.setText("");
        targetGraph.markTargetsAsChosen(addedTargetsList);
        Thread dataRefresherThread = new Thread(this::refreshTaskData);
        if (simulationTitledPane.isExpanded()) {
            lastTask = "Simulation";
            taskThread = new ExecutorThread(targetGraph, "Simulation", simulationWarningRateSpinner.getValue(),
                    simulationSuccessRateSpinner.getValue(), simulationRandomCheckBox.isSelected(),
                    simulationTimeSpinner.getValue(), ParallelismSpinner.getValue(), (!incrementalCheckBox.isDisabled() && incrementalCheckBox.isSelected()),runDetailsTextArea);
        }
        else {
            lastTask = "Compilation";
            taskThread = new ExecutorThread(targetGraph,"Compilation",compileTaskSourceTextField.getText(),
                compileTaskDestTextField.getText(),ParallelismSpinner.getValue(), (!incrementalCheckBox.isDisabled() && incrementalCheckBox.isSelected()),runDetailsTextArea);
        }
        taskThread.start();
        dataRefresherThread.start();
        createNewProgressBar();
        pauseTaskButton.setDisable(false);
        stopTaskButton.setDisable(false);
    }

    @FXML
    void selectAllButtonClicked(ActionEvent event) {
        TargetsListView.getSelectionModel().selectAll();
    }

    @FXML
    void stopTaskButtonClicked(ActionEvent event) {
        taskThread.setStopped(true);
        stopTaskButton.setDisable(true);
        pauseTaskButton.setDisable(true);
        isPaused.setValue(false);
        isIncrementalPossible.set(true);
        if (targetGraph.getAllTargets().values().stream().filter(Target::isChosen).allMatch
                (target -> (target.getRunResult() == Target.Result.SUCCESS ||
                        target.getRunResult() == Target.Result.WARNING))) {
            isIncrementalPossible.set(false);
        }
    }

    @FXML
    void pauseResumeTaskButtonClicked(ActionEvent event) {
        isPaused.setValue(!isPaused.getValue());
        taskThread.setPaused(isPaused.getValue());
    }

    @FXML
    void parallelismTextInputEntered(InputMethodEvent event) {
        System.out.println(event.getCommitted());
    }


    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        setAllTargetsNameList();
        ParallelValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, targetGraph.getMaxParallelism(), 1);
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
        if (dir != null && dir.isDirectory()) {
            lastVisitedDirectory = dir.getPath();
            compileTaskDestTextField.textProperty().setValue(dir.getPath());
        }
    }

    @FXML
    void compileTaskSourceSearchButtonClicked(ActionEvent event) {
        directoryChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File dir = directoryChooser.showDialog(compileTaskSourceSearchButton.getScene().getWindow());
        if (dir != null && dir.isDirectory()) {
            lastVisitedDirectory = dir.getPath();
            compileTaskSourceTextField.textProperty().setValue(dir.getPath());
        }
    }

    public void initializeTargetInfoTable() {
        name.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem, String>("Name"));
        type.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem, String>("Type"));
        serialSets.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem, Set<String>>("SerialSets"));
        status.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem, String>("Status"));
    }


    /*-------------------------------------------------data refreshing thread------------------------------------------------------------------------------------*/

    private void refreshTaskData() {
        while(taskThread.isAlive()){
            if(!isPaused.getValue()) {
            refreshTaskDataLists();
            }
        }
        refreshTaskDataLists();
        Platform.runLater(()->{
                pauseTaskButton.setDisable(true);
                isPaused.setValue(false);
                stopTaskButton.setDisable(true);
                isIncrementalPossible.set(true);
        });

        if (targetGraph.getAllTargets().values().stream().filter(Target::isChosen).allMatch
                (target -> (target.getRunResult() == Target.Result.SUCCESS ||
                        target.getRunResult() == Target.Result.WARNING))) {
            isIncrementalPossible.set(false);
        }
    }

    private void refreshTaskDataLists() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Platform.runLater(()->{
            clearTaskDataLists();
            updateTaskDataLists();
            this.FrozenListView.refresh();
            this.SkippedListView.refresh();
            this.WaitingListView.refresh();
            this.InProcessListView.refresh();
            this.FinishedListView.refresh();
        });
    }


    private void updateTaskDataLists() {
        for (Target target: targetGraph.getAllTargets().values().stream().filter(Target::isChosen)
                .filter(target -> target.getRunStatus().equals(Target.Status.FROZEN)).collect(Collectors.toSet())) {
            frozenTargetsNameList.add(target.getName());
        }
        for (Target target: targetGraph.getAllTargets().values().stream().filter(Target::isChosen)
                .filter(target -> target.getRunStatus().equals(Target.Status.SKIPPED)).collect(Collectors.toSet())) {
            skippedTargetsNameList.add(target.getName());
        }
        for (Target target: targetGraph.getAllTargets().values().stream().filter(Target::isChosen)
                .filter(target -> target.getRunStatus().equals(Target.Status.WAITING)).collect(Collectors.toSet())) {
            waitingTargetsNameList.add(target.getName());
        }
        for (Target target: targetGraph.getAllTargets().values().stream().filter(Target::isChosen)
                .filter(target -> target.getRunStatus().equals(Target.Status.IN_PROCESS)).collect(Collectors.toSet())) {
            inProcessTargetsNameList.add(target.getName());
        }
        for (Target target: targetGraph.getAllTargets().values().stream().filter(Target::isChosen)
                .filter(target -> target.getRunStatus().equals(Target.Status.FINISHED)).collect(Collectors.toSet())) {
            finishedTargetsNameList.add(target.getName() + " - " +  target.getRunResultAsString());
        }
    }

    private void clearTaskDataLists() {
        frozenTargetsNameList.clear();
        skippedTargetsNameList.clear();
        waitingTargetsNameList.clear();
        inProcessTargetsNameList.clear();
        finishedTargetsNameList.clear();
    }

    private void createNewProgressBar()
    {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int maxSize = targetGraph.getAllTargets().values().stream().filter(Target::isChosen).collect(Collectors.toSet()).size();
                while(taskThread.isAlive())
                {
                    Thread.sleep(200);
                    updateProgress(targetGraph.getAllTargets().values().stream().filter(Target::isChosen)
                            .filter(target -> target.getRunStatus().equals(Target.Status.FINISHED) ||
                                    target.getRunStatus().equals(Target.Status.SKIPPED)).collect(Collectors.toSet()).size(), maxSize);
                }
                updateProgress(maxSize,maxSize);
                return null;
            }
        };
        this.progressBar.progressProperty().bind(task.progressProperty());
        this.progressBarLabel.textProperty().bind
                (Bindings.concat(Bindings.format("%.0f", Bindings.multiply(task.progressProperty(), 100)), " %"));

        Thread progressBarThread = new Thread(task);
        progressBarThread.setDaemon(true);
        progressBarThread.start();
    }

    private void resetDataLists() {
        frozenTargetsNameList.clear();
        skippedTargetsNameList.clear();
        waitingTargetsNameList.clear();
        inProcessTargetsNameList.clear();
        finishedTargetsNameList.clear();
        FrozenListView.setItems(frozenTargetsNameList);
        SkippedListView.setItems(skippedTargetsNameList);
        WaitingListView.setItems(waitingTargetsNameList);
        InProcessListView.setItems(inProcessTargetsNameList);
        FinishedListView.setItems(finishedTargetsNameList);
    }
}