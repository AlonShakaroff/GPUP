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
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import util.http.HttpClientUtil;

import java.io.IOException;

public class TaskController {
    String userName;

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

    public void unregisterFromTask(String taskName) {
        RequestBody body = RequestBody.create("null", MediaType.parse("application/json"));
        String finalUrl = HttpUrl
                .parse(Constants.WORKER_TASK_PAGE)
                .newBuilder()
                .addQueryParameter("unregisterFromTask", "unregisterFromTask")
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
                response.close();
            }
        });
    }
}
