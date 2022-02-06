package dashboard;

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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import main.WorkerMainController;


public class DashboardController {

    private ObservableList<String> onlineTasksList = FXCollections.observableArrayList();
    private ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedTaskList = FXCollections.observableArrayList();

    private ListChangeListener<String> currentSelectedTaskListListener;

    private Stage primaryStage;
    private String userName;
    private Thread refreshDashboardDataThread;
    private WorkerMainController workerMainController;

    public DashboardController() {
        currentSelectedTaskListListener = change -> {
            //displaySelectedTaskInfo();
        };
    }

    @FXML
    private TitledPane OnlineAdminsTiltedPane;

    @FXML
    private ListView<?> OnlineAdminsListView;

    @FXML
    private TitledPane OnlineWorkersTiltedPane;

    @FXML
    private ListView<?> OnlineWorkersListView;

    @FXML
    private TitledPane OnlineTasksTiltedPane;

    @FXML
    private ListView<?> OnlineTasksListView;

    @FXML
    private Font x11;

    @FXML
    private Color x21;

    @FXML
    private TextField TaskNameTextField;

    @FXML
    private TextField UploadedByTextField;

    @FXML
    private TableView<?> TaskTypesAmountTableView;

    @FXML
    private TableColumn<?, ?> TargetsAmount;

    @FXML
    private TableColumn<?, ?> IndependentAmount;

    @FXML
    private TableColumn<?, ?> LeafAmount;

    @FXML
    private TableColumn<?, ?> MiddleAmount;

    @FXML
    private TableColumn<?, ?> RootAmount;

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

}
