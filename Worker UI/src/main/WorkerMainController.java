package main;

import constants.WorkersConstants;
import dashboard.DashboardController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tasks.control.TaskController;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static constants.WorkersConstants.WORKERS_TASKS_CONTROL_FXML_RESOURCE;
import static main.include.Constants.*;
import static main.include.Constants.DASHBOARD_FXML_RESOURCE;

public class WorkerMainController {

    @FXML private SplitPane runTaskComponent;
    @FXML private TaskController taskController;
    @FXML private SplitPane dashboardComponent;
    @FXML private DashboardController dashboardController;
    @FXML private VBox aboutComponent;

    private Stage aboutStage = null;
    private Stage primaryStage;
    private String userName;

    @FXML
    public void initialize(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        refreshComponentsAndControllers();

        dashboardController.setPrimaryStage(primaryStage);
        dashboardController.setUserName(userName);
    }

    @FXML private MenuItem classicSkinButton;
    @FXML private MenuItem chalkboardSkinButton;
    @FXML private MenuItem streetSkinButton;
    @FXML private MenuItem AviadSkinButton;
    @FXML private MenuItem aboutGPUPButton;
    @FXML private Font x1;
    @FXML private Color x2;
    @FXML private ToggleButton dashboardButton;
    @FXML private ToggleGroup mainoptions;
    @FXML private ToggleButton MyTasksButton;
    @FXML private ScrollPane mainChangingScene;
    @FXML private GridPane logoGridPane;
    @FXML private ImageView logoImageView;
    @FXML private VBox SlideBox2;
    @FXML private Font x3;
    @FXML private Color x4;

    @FXML
    void streetSkinButtonClicked(ActionEvent event) {
        primaryStage.getScene().getStylesheets().remove(0,1);
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource(WorkersConstants.STREET_CSS)).toExternalForm());
    }

    @FXML
    void AviadSkinButtonClicked(ActionEvent event) {
        primaryStage.getScene().getStylesheets().remove(0,1);
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource(WorkersConstants.AVIAD_CSS)).toExternalForm());
    }

    @FXML
    void chalkboardSkinButtonClicked(ActionEvent event) {
        primaryStage.getScene().getStylesheets().remove(0,1);
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource(WorkersConstants.CHALKBOARD_CSS)).toExternalForm());
    }
    @FXML
    void classicSkinButtonClicked(ActionEvent event) {
        primaryStage.getScene().getStylesheets().remove(0,1);
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource(WorkersConstants.CLASSIC_CSS)).toExternalForm());
    }

    @FXML
    void MyTasksButtonClicked(ActionEvent event) {

    }

    @FXML
    void dashboardButtonClicked(ActionEvent event) {

    }

    private void refreshComponentsAndControllers() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(WorkersConstants.WORKERS_TASKS_CONTROL_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        runTaskComponent = fxmlLoader.load(url.openStream());
        taskController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(WorkersConstants.WORKERS_DASHBOARD_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        dashboardComponent = fxmlLoader.load(url.openStream());
        dashboardController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(WorkersConstants.ABOUT_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        aboutComponent = fxmlLoader.load(url.openStream());
    }

    public void setUserName(String userName) { this.userName = userName; }
}
