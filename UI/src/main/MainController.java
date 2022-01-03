package main;

import connections.ConnectionsController;
import graph.GraphController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import runtask.TaskController;
import target.TargetGraph;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

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


    private final FileChooser fileChooser = new FileChooser();
    

    @FXML
    public void initialize(Stage primaryStage) throws IOException {

        this.primaryStage = primaryStage;

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
        menuBarCloseFileButton.setDisable(true);
        closeFileButton.setDisable(true);
        graphButton.setDisable(true);
        graphButton.setSelected(false);
        connectionsButton.setDisable(true);
        connectionsButton.setSelected(false);
        runTaskButton.setDisable(true);
        runTaskButton.setSelected(false);

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
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                TargetGraph.createTargetGraphFromXml(file);
                menuBarCloseFileButton.setDisable(false);
                closeFileButton.setDisable(false);
                graphButton.setDisable(false);
                connectionsButton.setDisable(false);
                runTaskButton.setDisable(false);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
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
