package dashboard;

import com.google.gson.Gson;
import dashboard.tableitems.TargetsInfoTableItem;
import dtos.GraphInfoDto;
import dtos.TaskDetailsDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
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

import java.io.IOException;
import java.util.Set;


public class DashboardController {

    private ObservableList<String> onlineTasksList = FXCollections.observableArrayList();
    private ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedTaskList = FXCollections.observableArrayList();
    private ObservableList<TargetsInfoTableItem> targetsTypeInfoTableList = FXCollections.observableArrayList();

    private ListChangeListener<String> currentSelectedTaskListListener;

    private Stage primaryStage;
    private String userName;
    private Thread refreshDashboardDataThread;
    private WorkerMainController workerMainController;

    public DashboardController() {
        currentSelectedTaskListListener = change -> { displaySelectedTaskInfo(); };
    }

    public void initialize() {
        currentSelectedTaskList = OnlineTasksListView.getSelectionModel().getSelectedItems();
        currentSelectedTaskList.addListener(currentSelectedTaskListListener);
        initializeTargetDetailsTable();
        refreshDashboardDataThread = new Thread(this::refreshDashboardData);
        Thread suddenExitHook = new Thread(this::logout);
        Runtime.getRuntime().addShutdownHook(suddenExitHook);
        OnlineAdminsListView.setItems(onlineAdminsList);
        OnlineWorkersListView.setItems(onlineWorkersList);
        OnlineTasksListView.setItems(onlineTasksList);
        refreshDashboardDataThread.setDaemon(true);
        refreshDashboardDataThread.start();
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
    private TableView<?> TaskInfoTableView;

    @FXML
    private TableColumn<?, ?> TaskStatus;

    @FXML
    private TableColumn<?, ?> AmountOfWorkers;

    @FXML
    private TableColumn<?, ?> TaskWorkPayment;

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

    }

    private void displaySelectedTaskInfo() {
        if (currentSelectedTaskList.isEmpty())
            return;

        String selectedTaskName = currentSelectedTaskList.get(0);
    }

    public void initializeTargetDetailsTable() {
        this.TargetsAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("targets"));
        this.RootAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("roots"));
        this.MiddleAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("middles"));
        this.LeafAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("leaves"));
        this.IndependentAmount.setCellValueFactory(new PropertyValueFactory<TargetsInfoTableItem, Integer>("independents"));
    }

    private void refreshDashboardData() {
        while (refreshDashboardDataThread.isAlive()) {
            getUsersLists();
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
    }

}
