package dashboard;

import javafx.application.Platform;
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
import util.http.HttpClientUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class dashboardController {

    private ObservableList<String> onlineGraphsList;
    private Stage primaryStage;
    private String userName;
    private final FileChooser fileChooser = new FileChooser();
    private static String lastVisitedDirectory = System.getProperty("user.home");

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


    @FXML void AddNewGraphButtonClicked(ActionEvent event) throws IOException {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TXT files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File file = fileChooser.showOpenDialog(primaryStage);

        if(file != null)
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
                Platform.runLater(()-> errorPopup(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if(response.code() < 200 || response.code() >= 300) {
                    System.out.println("got graph response - error: " + response.header("message"));
                    Platform.runLater(() -> errorPopup(response.header("message")));
                }
                else
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

}
