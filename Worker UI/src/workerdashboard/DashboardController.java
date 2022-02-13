package workerdashboard;

import com.google.gson.Gson;
import constants.WorkersConstants;
import dashboard.tableitems.SelectedTaskStatusTableItem;
import dashboard.tableitems.TargetsInfoTableItem;
import dtos.TaskDetailsDto;
import dtos.WorkerDetailsDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import main.WorkerMainController;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import users.UsersLists;
import util.http.HttpClientUtil;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class DashboardController {

    private ObservableList<String> onlineTasksList = FXCollections.observableArrayList();
    private ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedTaskList = FXCollections.observableArrayList();
    private ObservableList<TargetsInfoTableItem> targetsTypeInfoTableList = FXCollections.observableArrayList();
    private ObservableList<SelectedTaskStatusTableItem> TaskInfoTableList = FXCollections.observableArrayList();
    private SimpleBooleanProperty isRegisteredToCurTask;
    private int creditsEarned = 0;
    private Set<String> RegisteredTasks = new HashSet<>();

    private ListChangeListener<String> currentSelectedTaskListListener;

    private Stage primaryStage;
    private String userName;
    private Thread refreshDashboardDataThread;
    private WorkerMainController workerMainController;

    public DashboardController() {
        this.isRegisteredToCurTask = new SimpleBooleanProperty(true);
        currentSelectedTaskListListener = change -> {
            displaySelectedTaskInfo();
            if (change.getList().isEmpty())
                isRegisteredToCurTask.set(true);
        };
    }

    public void initialize() {
        currentSelectedTaskList = OnlineTasksListView.getSelectionModel().getSelectedItems();
        currentSelectedTaskList.addListener(currentSelectedTaskListListener);
        initializeTargetDetailsTable();
        initializeTaskStatusTable();
        refreshDashboardDataThread = new Thread(this::refreshDashboardData);
        Thread suddenExitHook = new Thread(this::logout);
        Runtime.getRuntime().addShutdownHook(suddenExitHook);
        OnlineAdminsListView.setItems(onlineAdminsList);
        OnlineWorkersListView.setItems(onlineWorkersList);
        OnlineTasksListView.setItems(onlineTasksList);
        refreshDashboardDataThread.setDaemon(true);
        refreshDashboardDataThread.start();
        JoinTaskButton.disableProperty().bind(isRegisteredToCurTask);
    }

    @FXML
    private TitledPane OnlineAdminsTiltedPane;

    @FXML
    private ListView<String> OnlineAdminsListView;

    @FXML
    private TitledPane OnlineWorkersTiltedPane;

    @FXML
    private ListView<String> OnlineWorkersListView;

    @FXML
    private TitledPane OnlineTasksTiltedPane;

    @FXML
    private ListView<String> OnlineTasksListView;

    @FXML
    private Font x11;

    @FXML
    private Color x21;

    @FXML
    private TextField TaskNameTextField;

    @FXML
    private TextField UploadedByTextField;

    @FXML
    private TableView<TargetsInfoTableItem> TaskTypesAmountTableView;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> TargetsAmount;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> IndependentAmount;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> LeafAmount;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> MiddleAmount;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> RootAmount;

    @FXML
    private TableView<SelectedTaskStatusTableItem> TaskInfoTableView;

    @FXML
    private TableColumn<SelectedTaskStatusTableItem, String> TaskStatus;

    @FXML
    private TableColumn<SelectedTaskStatusTableItem, Integer> currentWorkers;

    @FXML
    private TableColumn<SelectedTaskStatusTableItem, Integer> TaskWorkPayment;

    @FXML
    private TextField TaskTypeTextField;

    @FXML
    private TextField AmIRegisteredTextField;

    @FXML
    private Button JoinTaskButton;

    @FXML
    private Font x111;

    @FXML
    private Color x211;

    @FXML
    private TextField WorkerNameTextField;

    @FXML
    private TextField CreditsEarnedTextField;

    @FXML
    void JoinTaskButtonClicked(ActionEvent event) {
        String selectedItem = OnlineTasksListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            registerToTask(selectedItem);
            workerMainController.setSceneToTask();
        }
    }

    private void displaySelectedTaskInfo() {
        if (currentSelectedTaskList.isEmpty())
            return;

        String selectedTaskName = currentSelectedTaskList.get(0);

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
                            responseBody.close();
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
        this.UploadedByTextField.setText(taskDetailsDto.getUploader());
        this.AmIRegisteredTextField.setText(RegisteredTasks.contains(taskDetailsDto.getTaskName())? "Yes" : "No");
        this.isRegisteredToCurTask.set(RegisteredTasks.contains(taskDetailsDto.getTaskName()));
        this.TaskTypeTextField.setText(taskDetailsDto.getTaskTypeAsString());
        updateTaskDetailsTables(taskDetailsDto);
    }

    private void updateTaskDetailsTables(TaskDetailsDto taskDetailsDto) {

        TargetsInfoTableItem targetsInfoTableItem = new TargetsInfoTableItem(taskDetailsDto.getRoots(),
                taskDetailsDto.getMiddles(), taskDetailsDto.getLeaves(), taskDetailsDto.getIndependents(), taskDetailsDto.getTargets());
        SelectedTaskStatusTableItem selectedTaskStatusTableItem =
                new SelectedTaskStatusTableItem(taskDetailsDto.getTaskStatus(),taskDetailsDto.getTotalWorkers(),taskDetailsDto.getTotalPayment());

        this.targetsTypeInfoTableList.clear();
        this.targetsTypeInfoTableList.add(targetsInfoTableItem);
        this.TaskInfoTableList.clear();
        this.TaskInfoTableList.add(selectedTaskStatusTableItem);

        this.TaskTypesAmountTableView.setItems(this.targetsTypeInfoTableList);
        this.TaskInfoTableView.setItems(this.TaskInfoTableList);
    }


    public void initializeTargetDetailsTable() {
        this.TargetsAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("targets"));
        this.RootAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("roots"));
        this.MiddleAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("middles"));
        this.LeafAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("leaves"));
        this.IndependentAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("independents"));
    }

    public void initializeTaskStatusTable() {
        this.TaskStatus.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, String>("status"));
        this.currentWorkers.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, Integer>("workers"));
        this.TaskWorkPayment.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, Integer>("totalPayment"));
    }

    private void refreshDashboardData() {
        while (refreshDashboardDataThread.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getUsersLists();
            getOnlineTasksList();
            getCreditsEarnedAntRegisteredTasks();
            if (!currentSelectedTaskList.isEmpty()) {
                displaySelectedTaskInfo();
            }
        }
    }

    private void getCreditsEarnedAntRegisteredTasks() {

            String finalUrl = HttpUrl
                    .parse(WorkersConstants.GET_WORKER_PAGE)
                    .newBuilder()
                    .addQueryParameter("workerName", userName)
                    .addQueryParameter("getWorkerDto", "getWorkerDto")
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
                        if (responseBody != null) {
                            Platform.runLater(() -> {
                                try {
                                    WorkerDetailsDto workerDetailsDto = gson.fromJson(responseBody.string(), WorkerDetailsDto.class);
                                    if(workerDetailsDto != null) {
                                        responseBody.close();
                                        creditsEarned = workerDetailsDto.getEarnedCredits();
                                        CreditsEarnedTextField.setText(String.valueOf(creditsEarned));
                                        CreditsEarnedTextField.setText(String.valueOf(creditsEarned));
                                        RegisteredTasks.addAll(workerDetailsDto.getRegisteredTasks());
                                    }
                                    else {
                                        System.out.println("worker details dto failed to pass from server.");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    response.close();
                                }
                            });
                        }

                    } else
                        response.close();
                }
            });
        }

    private void getUsersLists() {
        String finalUrl = HttpUrl
                .parse(Constants.USERS_LISTS)
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Gson gson = new Gson();
                ResponseBody responseBody = response.body();
                UsersLists usersLists = gson.fromJson(responseBody.string(), UsersLists.class);
                responseBody.close();
                Platform.runLater(() -> {
                    updateUsersLists(usersLists);
                });
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }

    private void updateUsersLists(UsersLists usersLists) {
        onlineAdminsList.clear();
        onlineWorkersList.clear();
        onlineAdminsList.addAll(usersLists.getAdminsList());
        onlineWorkersList.addAll(usersLists.getWorkersList());
    }

    public void getOnlineTasksList() {
        String finalUrl = HttpUrl
                .parse(Constants.TASKS_LIST_PATH)
                .newBuilder()
                .addQueryParameter("onlineTasksList", "onlineTasksList")
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300)
                {
                    ResponseBody responseBody = response.body();
                    Gson gson = new Gson();
                    try {
                        if (responseBody != null) {
                            Set<String> taskList = gson.fromJson(responseBody.string(), Set.class);
                            Platform.runLater(() ->updateOnlineTasksList(taskList));
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

    private void updateOnlineTasksList(Set<String> taskList) {
        onlineTasksList.removeAll(onlineTasksList.stream().filter(task -> !taskList.contains(task)).collect(Collectors.toSet()));
        for (String task: taskList) {
            if(!onlineTasksList.contains(task))
                onlineTasksList.add(task);
        }
    }

    private void logout() {
        if (userName == null)
            return;

        String finalUrl = HttpUrl
                .parse(Constants.LOGOUT_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, "DELETE", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void updateTasksListView(Set<String> taskSet) {
        if (taskSet == null)
            return;

        for (String curr : taskSet) {
            if (!onlineTasksList.contains(curr))
                onlineTasksList.add(curr);
        }
    }

    private void updateTargetTypesDetailsTable(TaskDetailsDto graphInfoDto) {
        TargetsInfoTableItem targetsInfoTableItem = new TargetsInfoTableItem(graphInfoDto.getRoots(),
                graphInfoDto.getMiddles(), graphInfoDto.getLeaves(), graphInfoDto.getIndependents(), graphInfoDto.getTargets());

        this.targetsTypeInfoTableList.clear();
        this.targetsTypeInfoTableList.add(targetsInfoTableItem);

        DashboardController.this.TaskTypesAmountTableView.setItems(this.targetsTypeInfoTableList);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        WorkerNameTextField.setText(userName);
    }

    public void registerToTask(String taskName) {
        RequestBody body = RequestBody.create("null",MediaType.parse("application/json"));
        String finalUrl = HttpUrl
                .parse(Constants.WORKER_TASK_PAGE)
                .newBuilder()
                .addQueryParameter("registerToTask", "registerToTask")
                .addQueryParameter("taskName",taskName)
                .addQueryParameter("workerName", userName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "POST", body, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() > 300 || response.code() < 200) {
                    String message = response.header("message");
                    Platform.runLater(() -> errorPopup(message));
                }
                else
                    workerMainController.getTaskExecutor().addRegisteredTask(taskName);
                response.close();
            }
        });
    }

    public void errorPopup(String message) {
        Toolkit.getDefaultToolkit().beep();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.initOwner(primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
    }

    public void setWorkerMainController(WorkerMainController workerMainController) {
        this.workerMainController = workerMainController;
    }
}
