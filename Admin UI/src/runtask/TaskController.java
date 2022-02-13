package runtask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dashboard.tableitems.SelectedTaskStatusTableItem;
import dashboard.tableitems.TargetsInfoTableItem;
import dtos.TaskDetailsDto;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import main.AdminMainController;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import runtask.tableview.TargetInfoTableItem;
import target.Target;
import target.TargetForWorker;
import target.TargetGraph;
import util.http.HttpClientUtil;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskController {

    private TargetGraph targetGraph;
    private AdminMainController mainController;
    private String lastTask = "";
    private SimpleBooleanProperty isPaused;
    private final SimpleBooleanProperty isATargetSelected;
    private final SimpleBooleanProperty isIncrementalPossible;
    private final SimpleBooleanProperty isCurTaskFinished;

    private final ChangeListener<Boolean> isPausedListener;
    private final ListChangeListener<String> currentSelectedFrozenListener;
    private final ListChangeListener<String> currentSelectedSkippedListener;
    private final ListChangeListener<String> currentSelectedWaitingListener;
    private final ListChangeListener<String> currentSelectedInProcessListener;
    private final ListChangeListener<String> currentSelectedFinishedListener;
    private final ObservableList<String> TargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> frozenTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> skippedTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> waitingTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> inProcessTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> finishedTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<TargetInfoTableItem> targetInfoTableList = FXCollections.observableArrayList();
    private ObservableList<TargetsInfoTableItem> TaskInfoTargetTableList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedFrozenList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedSkippedList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedWaitingList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedInProcessList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedFinishedList = FXCollections.observableArrayList();

    private Integer currTaskAmountOfChosenTargets = 1;
    private Integer currTaskAmountOfFinishedTargets = 0;
    private Boolean isTaskFinished = false;
    private Thread progressBarThread = null;
    private Gson gson = new Gson();
    private Set<TargetForWorker> targetsDtoSet = new HashSet<>();

    public TaskController() {
        isPaused = new SimpleBooleanProperty(false);
        isCurTaskFinished = new SimpleBooleanProperty(false);
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

    }

    private void updateTargetDetailsTableAndTextArea(String selectedTargetString) {
        TargetForWorker selectedTarget = targetsDtoSet.stream()
                .filter(targetForWorker -> (targetForWorker.getName().equalsIgnoreCase(selectedTargetString.split(" ")[0]))).findFirst().orElse(null);
        targetInfoTableList.clear();
        targetInfoTableList.add(new TargetInfoTableItem(selectedTarget));
        TargetInfoTableView.setItems(targetInfoTableList);

        TargetInfoTextArea.clear();
        //statusUniqueDataDisplay(selectedTarget, selectedTarget.getRunStatus());
    }

    private void statusUniqueDataDisplay(Target selectedTarget, Target.Status status) {
        String uniqueData = "";
        switch (status) {
            case FROZEN:
                uniqueData = "Waiting for targets: \n" + selectedTarget.getDependsOnSet().stream()
                        .filter(Target::isChosen)
                        .filter(target -> (target.getRunStatus().equals(Target.Status.FROZEN) ||
                                target.getRunStatus().equals(Target.Status.WAITING) || target.getRunStatus().equals(Target.Status.IN_PROCESS)))
                        .collect(Collectors.toList()) + "\nto finish running successfully.";

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
                if (selectedTarget.getRunResult().equals(Target.Result.SUCCESS))
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
        runTaskButton.disableProperty().bind(Bindings.or(isCurTaskFinished, Bindings.and(stopTaskButton.disableProperty().not(),
                pauseTaskButton.disableProperty().not())));
        FrozenListView.setItems(frozenTargetsNameList);
        SkippedListView.setItems(skippedTargetsNameList);
        WaitingListView.setItems(waitingTargetsNameList);
        InProcessListView.setItems(inProcessTargetsNameList);
        FinishedListView.setItems(finishedTargetsNameList);
        initializeTaskTargetDetailsTable();
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
    private TableView<TargetsInfoTableItem> GraphTargetsTableView;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> GraphTargetsAmount;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> GraphIndependentAmount;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> GraphLeafAmount;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> GraphMiddleAmount;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> GraphRootAmount;

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
    private TextField TaskTypeTextField;


    @FXML
    void runTaskButtonClicked(ActionEvent event) {
        stopTaskButton.setDisable(false);
        pauseTaskButton.setDisable(false);
        setTaskRunStatus("In process");
        RequestBody body = RequestBody.create("", MediaType.parse("application/json"));
        String taskName = mainController.getTaskName();

        String finalUrl = HttpUrl
                .parse(Constants.TASKS_OPERATION_PATH)
                .newBuilder()
                .addQueryParameter("operation", "start")
                .build()
                .toString();
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(body)
                .addHeader("taskName", taskName)
                .build();


        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() == 202) {
                    Platform.runLater(() -> {
                        createNewProgressBar();
                    });
                }
                response.close();
            }

        });

        Thread dataRefresherThread = new Thread(this::refreshTaskData);
        dataRefresherThread.setDaemon(true);
        dataRefresherThread.start();
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
    }

    @FXML
    void stopTaskButtonClicked(ActionEvent event) {
        //taskThread.setStopped(true);
        stopTaskButton.setDisable(true);
        pauseTaskButton.setDisable(true);
        isPaused.setValue(false);
        setTaskRunStatus("Stopped");
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
        if (isPaused.getValue())
            setTaskRunStatus("Paused");
        else
            setTaskRunStatus("In process");
        //taskThread.setPaused(isPaused.getValue());
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

    private void displaySelectedTaskInfo(String selectedTaskName) {

        String finalUrl = HttpUrl
                .parse(Constants.TASKS_PATH)
                .newBuilder()
                .addQueryParameter("selectedTaskName", selectedTaskName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    ResponseBody responseBody = response.body();
                    Gson gson = new Gson();
                    try {
                        if (responseBody != null) {
                            TaskDetailsDto taskDetailsDto = gson.fromJson(responseBody.string(), TaskDetailsDto.class);
                            Platform.runLater(() -> displaySelectedTaskInfoFromDto(taskDetailsDto));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }

    private void displaySelectedTaskInfoFromDto(TaskDetailsDto taskDetailsDto) {
        this.TaskNameTextField.setText(taskDetailsDto.getTaskName());
        this.CurrentWorkersTextField.setText(taskDetailsDto.getTotalWorkers().toString());
        this.TaskOnGraphTextField.setText(taskDetailsDto.getGraphName());
        this.TaskTypeTextField.setText(taskDetailsDto.getTaskType().toString());

        TargetsInfoTableItem targetsInfoTableItem = new TargetsInfoTableItem(taskDetailsDto.getRoots(),
                taskDetailsDto.getMiddles(), taskDetailsDto.getLeaves(), taskDetailsDto.getIndependents(), taskDetailsDto.getTargets());
        this.TaskInfoTargetTableList.clear();
        this.TaskInfoTargetTableList.add(targetsInfoTableItem);

        this.GraphTargetsTableView.setItems(TaskInfoTargetTableList);
    }


    public void setNewTask() {
        String taskName = mainController.getTaskName();
        displaySelectedTaskInfo(taskName);
    }

    public void setMainController(AdminMainController mainController) {
        this.mainController = mainController;
    }

    public void initializeTaskTargetDetailsTable() {
        this.GraphTargetsAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("targets"));
        this.GraphRootAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("roots"));
        this.GraphMiddleAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("middles"));
        this.GraphLeafAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("leaves"));
        this.GraphIndependentAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("independents"));
    }

    /*-------------------------------------------------data refreshing thread------------------------------------------------------------------------------------*/

    private void refreshTaskData() {
        refreshTaskDataLists();
        while (!getTaskFinished()) {
            if (!isPaused.get())
                refreshTaskDataLists();
        }
        refreshTaskDataLists();
        Platform.runLater(() -> {
            pauseTaskButton.setDisable(true);
            isPaused.setValue(false);
            stopTaskButton.setDisable(true);
        });

        if (targetGraph.getAllTargets().values().stream().filter(Target::isChosen).allMatch
                (target -> (target.getRunResult() == Target.Result.SUCCESS ||
                        target.getRunResult() == Target.Result.WARNING))) {
            isIncrementalPossible.set(false);
        }
    }

    public void refreshTaskDataLists() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        updateTargetsDataFromServer();
    }

    public void updateTargetsDataFromServer() {
        String finalUrl = HttpUrl
                .parse(Constants.GET_TARGETS_PAGE)
                .newBuilder()
                .addQueryParameter("taskName", mainController.getTaskName())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                targetsDtoSet = gson.fromJson(Objects.requireNonNull(response.body()).string(),
                        new TypeToken<Set<TargetForWorker>>() {
                        }.getType());
                Objects.requireNonNull(response.body()).close();
                Platform.runLater(() -> {
                    clearTaskDataLists();
                    updateTaskDataLists();
                });
                response.close();
            }
        });

    }

    private void updateTaskDataLists() {
        for (TargetForWorker target : targetsDtoSet.stream().filter(target -> target.getTargetStatus().equals(Target.Status.FROZEN)).collect(Collectors.toSet())) {
            frozenTargetsNameList.add(target.getName());
        }
        for (TargetForWorker target : targetsDtoSet.stream().filter(target -> target.getTargetStatus().equals(Target.Status.SKIPPED)).collect(Collectors.toSet())) {
            skippedTargetsNameList.add(target.getName());
        }
        for (TargetForWorker target : targetsDtoSet.stream().filter(target -> target.getTargetStatus().equals(Target.Status.WAITING)).collect(Collectors.toSet())) {
            waitingTargetsNameList.add(target.getName());
        }
        for (TargetForWorker target : targetsDtoSet.stream().filter(target -> target.getTargetStatus().equals(Target.Status.IN_PROCESS)).collect(Collectors.toSet())) {
            inProcessTargetsNameList.add(target.getName());
        }
        for (TargetForWorker target : targetsDtoSet.stream().filter(target -> target.getTargetStatus().equals(Target.Status.FINISHED)).collect(Collectors.toSet())) {
            finishedTargetsNameList.add(target.getName() + " - " + target.getResult());
        }

        if ((finishedTargetsNameList.size() + skippedTargetsNameList.size()) == targetsDtoSet.size())
            setTaskFinished(true);

        frozenTargetsNameList.sort(String::compareTo);
        skippedTargetsNameList.sort(String::compareTo);
        waitingTargetsNameList.sort(String::compareTo);
        inProcessTargetsNameList.sort(String::compareTo);
        finishedTargetsNameList.sort(String::compareTo);
    }

    public void clearTaskDataLists() {
        frozenTargetsNameList.clear();
        skippedTargetsNameList.clear();
        waitingTargetsNameList.clear();
        inProcessTargetsNameList.clear();
        finishedTargetsNameList.clear();
    }

    private void createNewProgressBar() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgressFromServer();
                while (currTaskAmountOfFinishedTargets < currTaskAmountOfChosenTargets) {
                    Thread.sleep(300);
                    updateProgressFromServer();
                    updateProgress(currTaskAmountOfFinishedTargets, currTaskAmountOfChosenTargets);
                }
                updateProgress(currTaskAmountOfFinishedTargets, currTaskAmountOfChosenTargets);
                return null;
            }
        };
        this.progressBar.progressProperty().bind(task.progressProperty());
        this.progressBarLabel.textProperty().bind
                (Bindings.concat(Bindings.format("%.0f", Bindings.multiply(task.progressProperty(), 100)), " %"));

        if (progressBarThread != null)
            progressBarThread.interrupt();
        this.progressBarThread = new Thread(task);
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

    public void setStopAndPauseInit() {
        String finalUrl = HttpUrl
                .parse(Constants.TASKS_PATH)
                .newBuilder()
                .addQueryParameter("selectedTaskName", mainController.getTaskName())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    Platform.runLater(() ->
                            {
                                Gson gson = new Gson();
                                ResponseBody responseBody = response.body();
                                try {
                                    if (responseBody != null) {
                                        TaskDetailsDto taskDetailsDto = gson.fromJson(responseBody.string(), TaskDetailsDto.class);
                                        responseBody.close();
                                        switch (taskDetailsDto.getTaskStatus()) {
                                            case "Stopped":
                                            case "New":
                                            case "Finished":
                                                stopTaskButton.setDisable(true);
                                                pauseTaskButton.setDisable(true);
                                                pauseTaskButton.setText("pause");
                                                isPaused.setValue(false);
                                                break;
                                            case "Paused":
                                                stopTaskButton.setDisable(false);
                                                pauseTaskButton.setDisable(false);
                                                pauseTaskButton.setText("resume");
                                                isPaused.setValue(true);
                                                break;
                                            case "In process":
                                                stopTaskButton.setDisable(false);
                                                pauseTaskButton.setDisable(false);
                                                pauseTaskButton.setText("pause");
                                                isPaused.setValue(false);
                                                break;
                                        }
                                        createNewProgressBar();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
                }
            }
        });
    }

    private void setTaskRunStatus(String runStatus) {
        RequestBody body = RequestBody.create("", MediaType.parse("application/json"));
        String finalUrl = HttpUrl
                .parse(Constants.TASKS_PATH)
                .newBuilder()
                .addQueryParameter("selectedTaskName", mainController.getTaskName())
                .addQueryParameter("status", runStatus)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "POST", body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void updateProgressFromServer() {
        String finalUrl = HttpUrl
                .parse(Constants.TASKS_PATH)
                .newBuilder()
                .addQueryParameter("getProgress", mainController.getTaskName())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    currTaskAmountOfChosenTargets = (Integer.parseInt(Objects.requireNonNull(response.header("amountOfChosenTargets"))));
                    currTaskAmountOfFinishedTargets = (Integer.parseInt(Objects.requireNonNull(response.header("amountOfFinishedOrSkipped"))));
                    isTaskFinished = (Boolean.parseBoolean(response.header("isFinished")));
                }
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }

    public Integer getCurrTaskAmountOfChosenTargets() {
        return currTaskAmountOfChosenTargets;
    }

    public void setCurrTaskAmountOfChosenTargets(Integer currTaskAmountOfChosenTargets) {
        this.currTaskAmountOfChosenTargets = currTaskAmountOfChosenTargets;
    }

    public Integer getCurrTaskAmountOfFinishedTargets() {
        return currTaskAmountOfFinishedTargets;
    }

    public void setCurrTaskAmountOfFinishedTargets(Integer currTaskAmountOfFinishedTargets) {
        this.currTaskAmountOfFinishedTargets = currTaskAmountOfFinishedTargets;
    }

    public Boolean getTaskFinished() {
        return isTaskFinished;
    }

    public void setTaskFinished(Boolean taskFinished) {
        isTaskFinished = taskFinished;
        setTaskRunStatus("Finished");
    }

    public void resetProgressBarValues() {
        currTaskAmountOfChosenTargets = 1;
        currTaskAmountOfFinishedTargets = 0;
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