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
import login.AdminLoginController;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import util.http.HttpClientUtil;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.include.Constants.LOGIN_FXML_RESOURCE;

public class AdminMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);

        primaryStage.getIcons().add(new Image("/resources/images/icon.png"));
        primaryStage.setTitle("G.P.U.P Admin application");
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(LOGIN_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        VBox loginComponent = fxmlLoader.load(url.openStream());
        AdminLoginController adminLoginController = fxmlLoader.getController();

        Scene scene = new Scene(loginComponent,400, 230);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("classic.css")).toExternalForm());
        primaryStage.setScene(scene);

        adminLoginController.initialize(primaryStage);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Close confirmation");
                alert.setHeaderText("Are you sure you want to exit?");

                alert.initOwner(primaryStage);
                Toolkit.getDefaultToolkit().beep();
                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == ButtonType.OK) {
                    logout(adminLoginController.getCurrentUser());
                    Platform.exit();
                    System.exit(0);
                }
                event.consume();
            }
        });
        primaryStage.show();
    }


    private void logout(String userName) {
        if(userName == null)
            return;

        String finalUrl = HttpUrl
                .parse(Constants.LOGOUT_PAGE)
                .newBuilder()
                .addQueryParameter("username",userName)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, "DELETE", null, new Callback() {
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
