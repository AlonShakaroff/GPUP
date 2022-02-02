package main;

import connections.ConnectionsController;
import dashboard.dashboardController;
import graph.GraphController;
import javafx.animation.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Duration;
import login.LoginController;
import runtask.TaskController;
import target.TargetGraph;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static main.include.Constants.*;

public class MainController {

    @FXML private ScrollPane connectionsComponent;
    @FXML private ConnectionsController connectionsController;
    @FXML private TabPane graphComponent;
    @FXML private GraphController graphController;
    @FXML private SplitPane runTaskComponent;
    @FXML private TaskController taskController;
    @FXML private SplitPane dashboardComponent;
    @FXML private dashboardController dashboardController;
    @FXML private VBox aboutComponent;

    private Stage aboutStage = null;
    private Stage primaryStage;
    private static String lastVisitedDirectory = System.getProperty("user.home");
    private RotateTransition rotate;
    private ScaleTransition scale;

    private final FileChooser fileChooser = new FileChooser();
    private SimpleBooleanProperty isFileSelected;
    private GridPane loginComponent;
    private LoginController logicController;
    private AnchorPane mainPanel;


    public MainController()
    {
        isFileSelected = new SimpleBooleanProperty(false);
    }

    @FXML
    public void initialize(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        menuBarCloseFileButton.disableProperty().bind(isFileSelected.not());
        closeFileButton.disableProperty().bind(isFileSelected.not());
        graphButton.disableProperty().bind(isFileSelected.not());
        connectionsButton.disableProperty().bind(isFileSelected.not());
        runTaskButton.disableProperty().bind(isFileSelected.not());

        refreshComponentsAndControllers();

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(ABOUT_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        aboutComponent = fxmlLoader.load(url.openStream());
        AnimationsOffButton.setDisable(true);

        rotate = new RotateTransition(Duration.seconds(4), logoImageView);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.rateProperty().bind(rotationSlider.valueProperty());

        scale = new ScaleTransition(Duration.seconds(4), newFileButton);
        scale.setCycleCount(Animation.INDEFINITE);
        scale.setByX(0.5);
        scale.setByY(0.5);
        scale.setAutoReverse(true);
        scale.rateProperty().bind(sizeSlider.valueProperty());

        SlideBox2.setVisible(false);
        SlideBox1.setVisible(false);
    }

    private void refreshComponentsAndControllers() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(CONNECTIONS_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        connectionsComponent = fxmlLoader.load(url.openStream());
        connectionsController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(GRAPH_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        graphComponent = fxmlLoader.load(url.openStream());
        graphController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(RUNTASK_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        runTaskComponent = fxmlLoader.load(url.openStream());
        taskController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(DASHBOARD_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        dashboardComponent = fxmlLoader.load(url.openStream());
        dashboardController = fxmlLoader.getController();
    }

    @FXML
    private GridPane logoGridPane;

    @FXML
    private MenuItem menuBarOpenButton;

    @FXML
    private MenuItem menuBarCloseFileButton;

    @FXML
    private MenuItem classicSkinButton;

    @FXML
    private MenuItem chalkboardSkinButton;

    @FXML
    private MenuItem streetSkinButton;

    @FXML
    private MenuItem AviadSkinButton;

    @FXML
    private MenuItem aboutGPUPButton;

    @FXML
    private Font x1;

    @FXML
    private Color x2;

    @FXML
    private Button newFileButton;

    @FXML
    private ToggleButton dashboardButton;

    @FXML
    private Button closeFileButton;

    @FXML
    private ToggleButton graphButton;

    @FXML
    private ToggleGroup mainoptions;

    @FXML
    private ToggleButton connectionsButton;

    @FXML
    private ToggleButton runTaskButton;

    @FXML
    private ScrollPane mainChangingScene;

    @FXML
    private ImageView logoImageView;

    @FXML
    private Font x3;

    @FXML
    private Color x4;

    @FXML
    private MenuItem AnimationsOnButton;

    @FXML
    private MenuItem AnimationsOffButton;


    @FXML
    void aboutGPUPButtonClicked(ActionEvent event) {
        if (aboutStage == null) {
            aboutStage = new Stage();
            Scene aboutScene = new Scene(aboutComponent, 500, 350);
            aboutStage.getIcons().add(new Image("resources/images/icon.png"));
            aboutStage.initOwner(primaryStage);
            aboutStage.setScene(aboutScene);
            aboutStage.initModality(Modality.APPLICATION_MODAL);
            aboutStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    aboutStage.hide();
                }
            });
        }
        aboutStage.show();
    }


    @FXML
    void closeFileButtonClicked(ActionEvent event) {
        //delete file//
        closeFile();
    }

    private void closeFile() {
        mainChangingScene.setContent(logoGridPane);
        graphButton.setSelected(false);
        connectionsButton.setSelected(false);
        runTaskButton.setSelected(false);
        isFileSelected.set(false);
    }

    @FXML
    void connectionsButtonClicked(ActionEvent event) {
        if (connectionsButton.isSelected())
            mainChangingScene.setContent(connectionsComponent);
        else
            mainChangingScene.setContent(logoGridPane);
    }

    @FXML
    void dashboardButtonClicked(ActionEvent event) {
        if (dashboardButton.isSelected())
            mainChangingScene.setContent(dashboardComponent);
        else
            mainChangingScene.setContent(logoGridPane);
    }

    @FXML
    void graphButtonClicked(ActionEvent event)  {
        if (graphButton.isSelected())
            mainChangingScene.setContent(graphComponent);
        else
            mainChangingScene.setContent(logoGridPane);
    }

    @FXML
    void menuBarCloseFileButtonClicked(ActionEvent event) {
        closeFile();
    }

    @FXML
    void menuBarOpenButtonClicked(ActionEvent event) {
        fileExplorerLoadXMLFile();
    }

    @FXML
    void newFileButtonClicked(ActionEvent event) {
        //load file//
        fileExplorerLoadXMLFile();
    }

    @FXML
    private VBox SlideBox1;

    @FXML
    private Slider rotationSlider;

    @FXML
    private VBox SlideBox2;

    @FXML
    private Slider sizeSlider;


    private void fileExplorerLoadXMLFile() {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TXT files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            lastVisitedDirectory = file.getParent();
            try {
                TargetGraph targetGraph = TargetGraph.createTargetGraphFromXml(file);
                refreshComponentsAndControllers();
                graphController.setTargetGraph(targetGraph);
                connectionsController.setTargetGraph(targetGraph);
                taskController.setTargetGraph(targetGraph);
                mainChangingScene.setContent(graphComponent);
                graphButton.setSelected(true);
                isFileSelected.set(true);
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Loading error");
                alert.setHeaderText(e.getMessage());
                alert.initOwner(primaryStage);
                Optional<ButtonType> result = alert.showAndWait();
            }
        }
    }

    @FXML
    void runTaskButtonClicked(ActionEvent event) {
        if (runTaskButton.isSelected())
            mainChangingScene.setContent(runTaskComponent);
        else
            mainChangingScene.setContent(logoGridPane);
    }


    @FXML
    void streetSkinButtonClicked(ActionEvent event) {
        primaryStage.getScene().getStylesheets().remove(0,1);
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("street.css")).toExternalForm());
    }

    @FXML
    void AviadSkinButtonClicked(ActionEvent event) {
        primaryStage.getScene().getStylesheets().remove(0,1);
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("aviad.css")).toExternalForm());
    }

    @FXML
    void chalkboardSkinButtonClicked(ActionEvent event) {
        primaryStage.getScene().getStylesheets().remove(0,1);
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("chalkBoard.css")).toExternalForm());
    }
    @FXML
    void classicSkinButtonClicked(ActionEvent event) {
        primaryStage.getScene().getStylesheets().remove(0,1);
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("classic.css")).toExternalForm());
    }

    @FXML
    void AnimationsOffButtonClicked(ActionEvent event) {
        AnimationsOffButton.setDisable(true);
        AnimationsOnButton.setDisable(false);
        SlideBox2.setVisible(false);
        SlideBox1.setVisible(false);
        rotate.jumpTo(Duration.millis(0));
        scale.jumpTo(Duration.millis(0));
        rotate.stop();
        scale.stop();

    }

    @FXML
    void AnimationsOnButtonClicked(ActionEvent event) {
        SlideBox2.setVisible(true);
        SlideBox1.setVisible(true);
        AnimationsOffButton.setDisable(false);
        AnimationsOnButton.setDisable(true);
        rotate.play();
        scale.play();
    }
}
