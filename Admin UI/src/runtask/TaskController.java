package runtask;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import runtask.tableview.TargetInfoTableItem;
import target.Target;
import target.TargetGraph;
import task.ExecutorThread;
import task.GPUPTask;

import java.util.Set;
import java.util.stream.Collectors;

public class TaskController {

    private TargetGraph targetGraph;
    private GPUPTask currentGPUPTask;
    private ExecutorThread taskThread;
    private Thread dataRefreshThread;
    private Task<Void> task;
    private String lastTask = "";
    private SimpleBooleanProperty isPaused;
    private final SimpleBooleanProperty isATargetSelected;
    private final SimpleBooleanProperty isIncrementalPossible;
    private final ChangeListener<Boolean> isPausedListener;
    private final ListChangeListener<String> currentSelectedFrozenListener;
    private final ListChangeListener<String> currentSelectedSkippedListener;
    private final ListChangeListener<String> currentSelectedWaitingListener;
    private final ListChangeListener<String> currentSelectedInProcessListener;
    private final ListChangeListener<String> currentSelectedFinishedListener;
    private final InvalidationListener incrementalCheckboxInvalidListener;
    private final ObservableList<String> TargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> frozenTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> skippedTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> waitingTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> inProcessTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> finishedTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<TargetInfoTableItem> targetInfoTableList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedFrozenList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedSkippedList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedWaitingList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedInProcessList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedFinishedList = FXCollections.observableArrayList();

    public TaskController() {
        isPaused = new SimpleBooleanProperty(false);
        isATargetSelected = new SimpleBooleanProperty(false);
        isIncrementalPossible = new SimpleBooleanProperty(false);
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

        incrementalCheckBox.disableProperty().bind(isIncrementalPossible.not());
        incrementalCheckBox.disableProperty().addListener(incrementalCheckboxInvalidListener);
        currentSelectedFrozenList = FrozenListView.getSelectionModel().getSelectedItems();
        currentSelectedSkippedList = SkippedListView.getSelectionModel().getSelectedItems();
        currentSelectedWaitingList = WaitingListView.getSelectionModel().getSelectedItems();
        currentSelectedInProcessList = InProcessListView.getSelectionModel().getSelectedItems();
        currentSelectedFinishedList = FinishedListView.getSelectionModel().getSelectedItems();
        currentSelectedFrozenList.addListener(currentSelectedFrozenListener);
        currentSelectedSkippedList.addListener(currentSelectedSkippedListener);
        currentSelectedWaitingList.addListener(currentSelectedWaitingListener);
        currentSelectedInProcessList.addListener(currentSelectedInProcessListener);
        currentSelectedFinishedList.addListener(currentSelectedFinishedListener);
        runTaskButton.disableProperty().bind(Bindings.and(stopTaskButton.disableProperty(),
                pauseTaskButton.disableProperty()));
        FrozenListView.setItems(frozenTargetsNameList);
        SkippedListView.setItems(skippedTargetsNameList);
        WaitingListView.setItems(waitingTargetsNameList);
        InProcessListView.setItems(inProcessTargetsNameList);
        FinishedListView.setItems(finishedTargetsNameList);
    }

    @FXML
    private TableView<TargetInfoTableItem> TargetInfoTableView;

    @FXML
    private TableColumn<TargetInfoTableItem, String> name;

    @FXML
    private TableColumn<TargetInfoTableItem, String> type;

    @FXML
    private TableColumn<TargetInfoTableItem, String> status;

    @FXML
    private TextArea TargetInfoTextArea;

    @FXML
    private TextField TaskNameTextField;

    @FXML
    private TextField CurrentWorkersTextField;

    @FXML
    private TextField TaskOnGraphTextField;

    @FXML
    private TableView<TargetInfoTableItem> GraphTargetsTableView;

    @FXML
    private TableColumn<TargetInfoTableItem, Integer > GraphTargetsAmount;

    @FXML
    private TableColumn<TargetInfoTableItem, Integer> GraphIndependentAmount;

    @FXML
    private TableColumn<TargetInfoTableItem, Integer > GraphLeafAmount;

    @FXML
    private TableColumn<TargetInfoTableItem, Integer> GraphMiddleAmount;

    @FXML
    private TableColumn<TargetInfoTableItem, Integer> GraphRootAmount;

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
    private TextArea runDetailsTextArea;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressBarLabel;


    @FXML
    void runTaskButtonClicked(ActionEvent event) {

    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
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

    private void setAllTargetsNameList() {
        TargetsNameList.clear();
        TargetsNameList.addAll(targetGraph.getAllTargets().keySet());
    }
    public void initializeTargetInfoTable() {
        name.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem, String>("Name"));
        type.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem, String>("Type"));
        status.setCellValueFactory(new PropertyValueFactory<TargetInfoTableItem, String>("Status"));
    }
    /*-------------------------------------------------data refreshing thread------------------------------------------------------------------------------------*/

    private void refreshTaskData() {
        while(taskThread.isAlive()){
            refreshTaskDataLists();
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


/** ------------------------------- Old Run Task --------------------------------
 @FXML
void runTaskButtonClicked(ActionEvent event) {
ParallelismSpinner.setDisable(true);
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
 **/