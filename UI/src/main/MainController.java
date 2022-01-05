package main;

import connections.ConnectionsController;
import graph.GraphController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import runtask.TaskController;
import target.TargetGraph;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import static main.include.CommonResourcesPaths.*;

public class MainController {

    @FXML private ScrollPane connectionsComponent;
    @FXML private ConnectionsController connectionsController;
    @FXML private TabPane graphComponent;
    @FXML private GraphController graphController;
    @FXML private SplitPane runTaskComponent;
    @FXML private TaskController taskController;
    @FXML private VBox aboutComponent;
    private Stage aboutStage = null;
    private Stage primaryStage;
    private static String lastVisitedDirectory = System.getProperty("user.home");


    private final FileChooser fileChooser = new FileChooser();
    private SimpleBooleanProperty isFileSelected;


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
        url = getClass().getResource(ABOUT_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        aboutComponent = fxmlLoader.load(url.openStream());
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

    private void fileExplorerLoadXMLFile() {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TXT files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            lastVisitedDirectory = file.getParent();
            try {
                TargetGraph targetGraph = TargetGraph.createTargetGraphFromXml(file);
                graphController.setTargetGraph(targetGraph);
                connectionsController.setTargetGraph(targetGraph);
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
}
