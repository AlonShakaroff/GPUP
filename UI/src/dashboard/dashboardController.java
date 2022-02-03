package dashboard;

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

public class dashboardController {

    @FXML
    private TitledPane OnlineGraphsTiltedPane;

    @FXML
    private ListView<?> OnlineGraphsListView;

    @FXML
    private Button AddNewGraphButton;

    @FXML
    private Button LoadGraphButton;

    @FXML
    private TitledPane OnlineAdminsTiltedPane;

    @FXML
    private ListView<?> onlineAdminsListView;

    @FXML
    private TitledPane OnlineWorkersTiltedPane;

    @FXML
    private ListView<?> onlineWorkersListView;

    @FXML
    private TitledPane OnlineTasksTiltedPane;

    @FXML
    private ListView<?> myTasksListView;

    @FXML
    private Button loadSelectedTaskButton;

    @FXML
    private ListView<?> AllTasksListView;

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
    void AddNewGraphButtonClicked(ActionEvent event) {

    }

    @FXML
    void LoadGraphButtonClicked(ActionEvent event) {

    }

    @FXML
    void loadSelectedTaskButtonClicked(ActionEvent event) {

    }

}
