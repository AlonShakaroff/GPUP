package tasks.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TaskController {

    @FXML
    private TitledPane OnlineTasksTiltedPane1;

    @FXML
    private ListView<?> MyTasksListView;

    @FXML
    private Font x111;

    @FXML
    private Color x211;

    @FXML
    private TextField TaskNameTextField;

    @FXML
    private TableView<?> TaskInfoTableView;

    @FXML
    private TableColumn<?, ?> TaskStatus;

    @FXML
    private TableColumn<?, ?> AmountOfWorkers;

    @FXML
    private TextField TargetsDoneTextField;

    @FXML
    private TextField TaskCreditsEarnedTextField;

    @FXML
    private Button PauseTaskButton;

    @FXML
    private Button QuitTaskButton;

    @FXML
    private Font x1111;

    @FXML
    private Color x2111;

    @FXML
    private ProgressBar TaskProgressBar;

    @FXML
    private TitledPane OnlineTasksTiltedPane;

    @FXML
    private ListView<?> MyTargetsListView;

    @FXML
    private Font x11;

    @FXML
    private Color x21;

    @FXML
    private TextField TargetNameTextField;

    @FXML
    private TextField TargetTaskNameTextField;

    @FXML
    private TextField TargetTaskTypeTextField;

    @FXML
    private TextField TargetStatusTextField;

    @FXML
    private TextField TargetCreditsEarnedTextField;

    @FXML
    private Font x112;

    @FXML
    private Color x212;

    @FXML
    private TextArea TargetsRunLogTextArea;

    @FXML
    void PauseTaskButtonClicked(ActionEvent event) {

    }

    @FXML
    void QuitTaskButtonClicked(ActionEvent event) {

    }

}
