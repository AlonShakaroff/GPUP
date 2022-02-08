package dashboard;

import com.google.gson.Gson;
import dashboard.tableitems.SelectedTaskStatusTableItem;
import dashboard.tableitems.TargetsInfoTableItem;
import dtos.GraphInfoDto;
import dtos.TaskDetailsDto;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.AdminMainController;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import users.UsersLists;
import util.http.HttpClientUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class DashboardController {
    private ObservableList<String> onlineGraphsList = FXCollections.observableArrayList();
    private ObservableList<String> allTasksList = FXCollections.observableArrayList();
    private ObservableList<String> myTasksList = FXCollections.observableArrayList();
    private ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedGraphList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedMyTasksList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedAllTasksList = FXCollections.observableArrayList();
    private ObservableList<TargetsInfoTableItem> graphInfoTableList = FXCollections.observableArrayList();
    private ObservableList<TargetsInfoTableItem> TaskInfoTargetTableList = FXCollections.observableArrayList();
    private ObservableList<SelectedTaskStatusTableItem> TaskGeneralInfoTableList = FXCollections.observableArrayList();
    private ListChangeListener<String> currentSelectedGraphListListener;
    private ListChangeListener<String> currentSelectedMyTasksListListener;
    private ListChangeListener<String> currentSelectedAllTasksListListener;
    private SimpleBooleanProperty isGraphSelected;
    private SimpleBooleanProperty isMyTaskSelected;
    private SimpleBooleanProperty isAllTaskSelectedAndRanAlready;
    private SimpleBooleanProperty selectedAllTaskFinished;
    private SimpleBooleanProperty selectedAllTaskCanRunIncrementally;

    private Stage primaryStage;
    private String userName;
    private final FileChooser fileChooser = new FileChooser();
    private static String lastVisitedDirectory = System.getProperty("user.home");
    private Thread refreshDashboardDataThread;
    private AdminMainController adminMainController;

    public DashboardController() {
        isGraphSelected = new SimpleBooleanProperty(false);
        isMyTaskSelected = new SimpleBooleanProperty(false);
        isAllTaskSelectedAndRanAlready = new SimpleBooleanProperty(false);
        selectedAllTaskFinished = new SimpleBooleanProperty(false);
        selectedAllTaskCanRunIncrementally = new SimpleBooleanProperty(false);

        currentSelectedGraphListListener = change -> {
            displaySelectedGraphInfo();
            isGraphSelected.setValue(change.getList().size() != 0);
        };
        currentSelectedMyTasksListListener = change -> {
            isMyTaskSelected.setValue(change.getList().size() != 0);
        };
        currentSelectedAllTasksListListener = change -> {
            displaySelectedTaskInfo();
            FromScratchRadioButton.setSelected(true);
            isAllTaskSelectedAndRanAlready.setValue(change.getList().size() != 0 && selectedAllTaskFinished.getValue());
        };
    }

    public void initialize() {
        IncrementalRadioButton.disableProperty().bind(Bindings.or(FromScratchRadioButton.disableProperty() , selectedAllTaskCanRunIncrementally.not()));
        FromScratchRadioButton.disableProperty().bind(ReloadTaskButton.disableProperty());
        ReloadTaskButton.disableProperty().bind(isAllTaskSelectedAndRanAlready.not());

        LoadGraphButton.disableProperty().bind(isGraphSelected.not());
        loadSelectedTaskButton.disableProperty().bind(isMyTaskSelected.not());
        currentSelectedGraphList = OnlineGraphsListView.getSelectionModel().getSelectedItems();
        currentSelectedMyTasksList = myTasksListView.getSelectionModel().getSelectedItems();
        currentSelectedAllTasksList = AllTasksListView.getSelectionModel().getSelectedItems();
        currentSelectedGraphList.addListener(currentSelectedGraphListListener);
        currentSelectedMyTasksList.addListener(currentSelectedMyTasksListListener);
        currentSelectedAllTasksList.addListener(currentSelectedAllTasksListListener);
        initializeTargetDetailsTable();
        initializeTaskTargetDetailsTable();
        initializeTaskStatusTable();
        refreshDashboardDataThread = new Thread(this::refreshDashboardData);
        Thread suddenExitHook = new Thread(this::logout);
        Runtime.getRuntime().addShutdownHook(suddenExitHook);
        onlineAdminsListView.setItems(onlineAdminsList);
        onlineWorkersListView.setItems(onlineWorkersList);
        OnlineGraphsListView.setItems(onlineGraphsList);
        AllTasksListView.setItems(allTasksList);
        myTasksListView.setItems(myTasksList);
        refreshDashboardDataThread.setDaemon(true);
        refreshDashboardDataThread.start();
    }

    @FXML
    private TitledPane OnlineGraphsTiltedPane;
    @FXML
    private ListView<String> OnlineGraphsListView;
    @FXML
    private Button AddNewGraphButton;
    @FXML
    private Button LoadGraphButton;
    @FXML
    private TitledPane OnlineAdminsTiltedPane;
    @FXML
    private ListView<String> onlineAdminsListView;
    @FXML
    private TitledPane OnlineWorkersTiltedPane;
    @FXML
    private ListView<String> onlineWorkersListView;
    @FXML
    private TitledPane OnlineTasksTiltedPane;
    @FXML
    private ListView<String> myTasksListView;
    @FXML
    private Button loadSelectedTaskButton;
    @FXML
    private ListView<String> AllTasksListView;
    @FXML
    private Font x11;
    @FXML
    private Color x21;
    @FXML
    private TextField GraphNameTextField;
    @FXML
    private TextField uploadedByTextField;
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
    private TextField SimulationPriceTextField;
    @FXML
    private TextField CompilationPriceTextField;
    @FXML
    private Font x1;
    @FXML
    private Color x2;
    @FXML
    private TextField TaskNameTextField;
    @FXML
    private TextField CreatedByTextField;
    @FXML
    private TextField TaskOnGraphTextField;
    @FXML
    private TextField TaskTypeTextField;
    @FXML
    private TableView<TargetsInfoTableItem> TaskTypeTableView;
    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> TaskTargetsAmount;
    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> TaskIndependentAmount;
    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> TaskLeafAmount;
    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> TaskMiddleAmount;
    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> TaskRootAmount;
    @FXML
    private TableView<SelectedTaskStatusTableItem> TaskInfoTableView;
    @FXML
    private TableColumn<SelectedTaskStatusTableItem, String> TaskStatus;
    @FXML
    private TableColumn<SelectedTaskStatusTableItem, Integer> currentWorkers;
    @FXML
    private TableColumn<SelectedTaskStatusTableItem, Integer> TaskWorkPayment;
    @FXML
    private Button ReloadTaskButton;
    @FXML
    private RadioButton FromScratchRadioButton;
    @FXML
    private ToggleGroup increment;
    @FXML
    private RadioButton IncrementalRadioButton;


    @FXML
    void ReloadTaskButtonClicked(ActionEvent event) {
        adminMainController.setCurTaskIncremental(IncrementalRadioButton.isSelected());
        String selectedReloadTaskName = this.myTasksListView.getSelectionModel().getSelectedItem();
        String NewTaskName;
        int copyNum = 0;
        do{
            NewTaskName = selectedReloadTaskName.concat("(" + copyNum + ")");
            copyNum++;
        }while (allTasksList.contains(NewTaskName));

        adminMainController.createIncrementalTask(NewTaskName, selectedReloadTaskName, IncrementalRadioButton.isSelected());
        adminMainController.setSelectedTaskTextField(NewTaskName);
        this.AllTasksListView.getSelectionModel().clearSelection();
        adminMainController.setSceneToTask();
    }

        @FXML
    void AddNewGraphButtonClicked(ActionEvent event) throws IOException {
        addNewGraphToList();
    }

    public void addNewGraphToList() throws IOException {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TXT files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            uploadFileToServer(Constants.GRAPHS_PATH, file);
            lastVisitedDirectory = file.getParent();
        }
    }

    public void uploadFileToServer(String url, File file) throws IOException {
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("fileToUpload", file.getName(),
                        RequestBody.create(file, MediaType.parse("xml")))
                .build();

        Request request = new Request.Builder()
                .url(Constants.GRAPHS_PATH)
                .post(body).addHeader("username", this.userName)
                .build();

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorPopup(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() < 200 || response.code() >= 300) {
                    Platform.runLater(() -> errorPopup(response.header("message")));
                }
            }
        });
    }

    @FXML
    void LoadGraphButtonClicked(ActionEvent event) {
        String selectedGraphName = this.OnlineGraphsListView.getSelectionModel().getSelectedItem();

        if(selectedGraphName == null)
            return;

        adminMainController.setSelectedGraphTextField(selectedGraphName);
        this.OnlineGraphsListView.getSelectionModel().clearSelection();
        String finalUrl = HttpUrl
                .parse(Constants.GRAPHS_PATH)
                .newBuilder()
                .addQueryParameter("graph", selectedGraphName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorPopup(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    Gson gson = new Gson();
                    ResponseBody responseBody = response.body();
                    File graphXMLFile = gson.fromJson(responseBody.string(), File.class);
                    responseBody.close();
                    Platform.runLater(()-> adminMainController.LoadXMLFile(graphXMLFile));
                } else //Failed
                {
                    Platform.runLater(() -> errorPopup(response.message()));
                }
            }
        });
    }

    @FXML
    void loadSelectedTaskButtonClicked(ActionEvent event) {
        String selectedTaskName = this.myTasksListView.getSelectionModel().getSelectedItem();

        if(selectedTaskName == null)
            return;

        adminMainController.setSelectedTaskTextField(selectedTaskName);
        adminMainController.setCurTaskIncremental(false);
        this.myTasksListView.getSelectionModel().clearSelection();
        adminMainController.setSceneToTask();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void errorPopup(String message) {
        Toolkit.getDefaultToolkit().beep();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Loading error");
        alert.setHeaderText(message);
        alert.initOwner(primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
    }

    private void refreshDashboardData() {
        while (refreshDashboardDataThread.isAlive()) {
            getUsersLists();
            refreshGraphList();
            refreshTaskLists();
        }
    }

    private void getUsersLists() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            }
        });
    }

    private void updateUsersLists(UsersLists usersLists) {
        onlineAdminsList.clear();
        onlineWorkersList.clear();
        onlineAdminsList.addAll(usersLists.getAdminsList());
        onlineWorkersList.addAll(usersLists.getWorkersList());
    }

    public void initializeTargetDetailsTable() {
        this.GraphTargetsAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("targets"));
        this.GraphRootAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("roots"));
        this.GraphMiddleAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("middles"));
        this.GraphLeafAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("leaves"));
        this.GraphIndependentAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("independents"));
    }

    public void initializeTaskTargetDetailsTable() {
        this.TaskTargetsAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("targets"));
        this.TaskRootAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("roots"));
        this.TaskMiddleAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("middles"));
        this.TaskLeafAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("leaves"));
        this.TaskIndependentAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("independents"));
    }

    public void initializeTaskStatusTable() {
        this.TaskStatus.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, String>("status"));
        this.currentWorkers.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, Integer>("workers"));
        this.TaskWorkPayment.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, Integer>("totalPayment"));
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
            }
        });
    }

    private void refreshGraphList() {
        String finalUrl = HttpUrl
                .parse(Constants.GRAPHS_LISTS_PAGE)
                .newBuilder()
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
                                        Set graphsSet = gson.fromJson(responseBody.string(), Set.class);
                                        responseBody.close();
                                        updateGraphListView(graphsSet);
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

    private void updateGraphListView(Set<String> graphsSet) {
        if (graphsSet == null)
            return;

        for (String curr : graphsSet) {
            if (!onlineGraphsList.contains(curr))
                onlineGraphsList.add(curr);
        }
    }

    private void displaySelectedTaskInfo() {
        if (currentSelectedAllTasksList.isEmpty())
            return;

        String selectedTaskName = currentSelectedAllTasksList.get(0);

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
                    Platform.runLater(() ->
                            {
                                Gson gson = new Gson();
                                ResponseBody responseBody = response.body();
                                try {
                                    if (responseBody != null) {
                                        TaskDetailsDto taskDetailsDto = gson.fromJson(responseBody.string(), TaskDetailsDto.class);
                                        responseBody.close();

                                        //selectedAllTaskCanRunIncrementally.setValue(taskDetailsDto.getCanRunIncrementally());
                                        selectedAllTaskFinished.setValue(taskDetailsDto.getTaskStatus().equals("Finished"));

                                        displaySelectedTaskInfoFromDto(taskDetailsDto);
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

    private void displaySelectedTaskInfoFromDto(TaskDetailsDto taskDetailsDto) {
        this.TaskNameTextField.setText(taskDetailsDto.getTaskName());
        this.CreatedByTextField.setText(taskDetailsDto.getUploader());
        this.TaskOnGraphTextField.setText(taskDetailsDto.getGraphName());
        this.TaskTypeTextField.setText(taskDetailsDto.getTaskTypeAsString());
        updateTaskDetailsTables(taskDetailsDto);
    }

    private void updateTaskDetailsTables(TaskDetailsDto taskDetailsDto) {

        TargetsInfoTableItem targetsInfoTableItem = new TargetsInfoTableItem(taskDetailsDto.getRoots(),
                taskDetailsDto.getMiddles(), taskDetailsDto.getLeaves(), taskDetailsDto.getIndependents(), taskDetailsDto.getTargets());
        SelectedTaskStatusTableItem selectedTaskStatusTableItem =
                new SelectedTaskStatusTableItem(taskDetailsDto.getTaskStatus(),taskDetailsDto.getTotalWorkers(),taskDetailsDto.getTotalPayment());

        this.TaskInfoTargetTableList.clear();
        this.TaskInfoTargetTableList.add(targetsInfoTableItem);
        this.TaskGeneralInfoTableList.clear();
        this.TaskGeneralInfoTableList.add(selectedTaskStatusTableItem);

        this.TaskTypeTableView.setItems(this.TaskInfoTargetTableList);
        this.TaskInfoTableView.setItems(this.TaskGeneralInfoTableList);
    }

    private void displaySelectedGraphInfo() {
        if (currentSelectedGraphList.isEmpty())
            return;

        String selectedGraphName = currentSelectedGraphList.get(0);

        String finalUrl = HttpUrl
                .parse(Constants.GRAPHS_PATH)
                .newBuilder()
                .addQueryParameter("selectedGraphName", selectedGraphName)
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
                                        GraphInfoDto graphInfoDto = gson.fromJson(responseBody.string(), GraphInfoDto.class);
                                        responseBody.close();
                                        refreshGraphDetailsDTO(graphInfoDto);
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

    private void refreshGraphDetailsDTO(GraphInfoDto graphInfoDto) {
        this.GraphNameTextField.setText(graphInfoDto.getGraphName());
        this.uploadedByTextField.setText(graphInfoDto.getUploader());
        this.SimulationPriceTextField.setText(graphInfoDto.getSimulationPrice().toString());
        this.CompilationPriceTextField.setText(graphInfoDto.getCompilationPrice().toString());

        updateGraphTargetDetailsTable(graphInfoDto);
    }

    private void updateGraphTargetDetailsTable(GraphInfoDto graphInfoDto) {

        TargetsInfoTableItem targetsInfoTableItem = new TargetsInfoTableItem(graphInfoDto.getRoots(),
                graphInfoDto.getMiddles(), graphInfoDto.getLeaves(), graphInfoDto.getIndependents(), graphInfoDto.getTargets());

        this.graphInfoTableList.clear();
        this.graphInfoTableList.add(targetsInfoTableItem);

        DashboardController.this.GraphTargetsTableView.setItems(this.graphInfoTableList);
    }

    public AdminMainController getMainController() {
        return adminMainController;
    }

    public void setMainController(AdminMainController adminMainController) {
        this.adminMainController = adminMainController;
    }


    private void refreshTaskLists() {
        refreshAllTasksList();
        refreshMyTasksList();
    }

    private void refreshAllTasksList() {
        String finalUrl = HttpUrl
                .parse(Constants.TASKS_LIST_PATH)
                .newBuilder()
                .addQueryParameter("allTasksList", "allTasksList")
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
                    Platform.runLater(() ->
                            {
                                Gson gson = new Gson();
                                ResponseBody responseBody = response.body();
                                try {
                                    if (responseBody != null) {
                                        Set taskList = gson.fromJson(responseBody.string(), Set.class);
                                        updateAllTasksList(taskList);
                                        responseBody.close();
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

    private void updateAllTasksList(Set<String> allTasksList) {
        if(allTasksList == null)
            return;

        for(String curr : allTasksList)
        {
            if(!DashboardController.this.allTasksList.contains(curr))
                DashboardController.this.allTasksList.add(curr);
        }
    }


    private void refreshMyTasksList() {
        String finalUrl = HttpUrl
                .parse(Constants.TASKS_LIST_PATH)
                .newBuilder()
                .addQueryParameter("myTasksList", "myTasksList")
                .addQueryParameter("username", this.userName)
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
                                        Set taskList = gson.fromJson(responseBody.string(), Set.class);
                                        updateMyTasksList(taskList);
                                        responseBody.close();
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

    private void updateMyTasksList(Set<String> myTasksList) {
        if(myTasksList == null)
            return;

        for(String curr : myTasksList)
        {
            if(!DashboardController.this.myTasksList.contains(curr))
                DashboardController.this.myTasksList.add(curr);
        }
    }

    public void expandTaskTitledPane() {
        OnlineTasksTiltedPane.setExpanded(true);
    }

    public boolean getSelectedAllTaskCanRunIncrementally() {
        return selectedAllTaskCanRunIncrementally.get();
    }

    public SimpleBooleanProperty selectedAllTaskCanRunIncrementallyProperty() {
        return selectedAllTaskCanRunIncrementally;
    }

    public void setSelectedAllTaskCanRunIncrementally(boolean selectedAllTaskCanRunIncrementally) {
        this.selectedAllTaskCanRunIncrementally.set(selectedAllTaskCanRunIncrementally);
    }
}
