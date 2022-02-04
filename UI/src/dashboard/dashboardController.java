package dashboard;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import users.UsersLists;
import util.http.HttpClientUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class dashboardController {
    private ObservableList<String> onlineGraphsList = FXCollections.observableArrayList();
    private ObservableList<String> onlineTasksList = FXCollections.observableArrayList();
    private ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();

    private Stage primaryStage;
    private String userName;
    private final FileChooser fileChooser = new FileChooser();
    private static String lastVisitedDirectory = System.getProperty("user.home");
    private Thread usersListsRefreshThread;

    public void initialize() {
        usersListsRefreshThread = new Thread(this::refreshUsersLists);
        Thread suddenExitHook = new Thread(this::logout);
        Runtime.getRuntime().addShutdownHook(suddenExitHook);
        onlineAdminsListView.setItems(onlineAdminsList);
        onlineWorkersListView.setItems(onlineWorkersList);
        usersListsRefreshThread.setDaemon(true);
        usersListsRefreshThread.start();
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
    private TableView<?> GraphTargetsTableView;

    @FXML
    private TableColumn<?, ?> GraphTargetsAmount;

    @FXML
    private TableColumn<?, ?> GraphIndependentAmount;

    @FXML
    private TableColumn<?, ?> GraphLeafAmount;

    @FXML
    private TableColumn<?, ?> GraphMiddleAmount;

    @FXML
    private TableColumn<?, ?> GraphRootAmount;

    @FXML
    private TextField GraphNameTextField1;

    @FXML
    private TextField GraphNameTextField11;

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
    private TableView<?> typeTableView1;

    @FXML
    private TableColumn<?, ?> TaskTargetsAmount;

    @FXML
    private TableColumn<?, ?> TaskIndependentAmount;

    @FXML
    private TableColumn<?, ?> TaskLeafAmount;

    @FXML
    private TableColumn<?, ?> TaskMiddleAmount;

    @FXML
    private TableColumn<?, ?> TaskRootAmount;

    @FXML
    private TableView<?> typeTableView11;

    @FXML
    private TableColumn<?, ?> TaskStatus;

    @FXML
    private TableColumn<?, ?> currentWorkers;

    @FXML
    private TableColumn<?, ?> TaskWorkPayment;


    @FXML
    void AddNewGraphButtonClicked(ActionEvent event) throws IOException {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TXT files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null)
            uploadFileToServer(Constants.GRAPHS_PATH, file);
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

        System.out.println("making a graph request");

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("got graph response - failed");
                Platform.runLater(() -> errorPopup(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() < 200 || response.code() >= 300) {
                    System.out.println("got graph response - error: " + response.header("message"));
                    Platform.runLater(() -> errorPopup(response.header("message")));
                } else
                    System.out.println("got graph response - success");
            }
        });

        System.out.println("sent async request");
//        Response response = client.newCall(request).execute();
    }

    @FXML
    void LoadGraphButtonClicked(ActionEvent event) {

    }

    @FXML
    void loadSelectedTaskButtonClicked(ActionEvent event) {

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

    private void refreshUsersLists() {
        while (usersListsRefreshThread.isAlive()) {
            getUsersLists();
        }
    }

    private void getUsersLists() {
        try {
            System.out.println("going to sleep for 1 second");
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
                System.out.println("I failed you master");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println("responding to refresh call");
                Gson gson = new Gson();
                ResponseBody responseBody = response.body();
                UsersLists usersLists = gson.fromJson(responseBody.string(), UsersLists.class);
                if(usersLists.getAdminsList().isEmpty())
                    System.out.println("admins list is empty");
                else
                    System.out.println("there are admins in the list");
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
        if(userName == null)
            return;

        String finalUrl = HttpUrl
                .parse(Constants.LOGOUT_PAGE)
                .newBuilder()
                .addQueryParameter("username",userName)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, "DELETE", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("failed to logout user");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println("user logged out");
            }
        });
    }
}
