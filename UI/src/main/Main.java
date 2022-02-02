package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import login.LoginController;

import java.awt.*;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import static main.include.Constants.LOGIN_FXML_RESOURCE;
import static main.include.Constants.MAIN_FXML_RESOURCE;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image("/resources/images/icon.png"));
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(LOGIN_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        GridPane loginComponent = fxmlLoader.load(url.openStream());
        LoginController loginController = fxmlLoader.getController();

        Scene scene = new Scene(loginComponent,400, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("classic.css")).toExternalForm());
        primaryStage.setScene(scene);

        loginController.initialize(primaryStage);

//        URL url = getClass().getResource(MAIN_FXML_RESOURCE);
//        fxmlLoader.setLocation(url);
//        VBox mainMenuComponent = fxmlLoader.load(url.openStream());
//        MainController mainController = fxmlLoader.getController();
//
//        Scene scene = new Scene(mainMenuComponent,1280, 800);
//        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("classic.css")).toExternalForm());
//        primaryStage.setScene(scene);
//
//        mainController.initialize(primaryStage);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Close confirmation");
                alert.setHeaderText("Are you sure you want to exit?");

                alert.initOwner(primaryStage);
                Toolkit.getDefaultToolkit().beep();
                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == ButtonType.OK)
                    Platform.exit();
                event.consume();
            }
        });

        primaryStage.show();
    }
}
