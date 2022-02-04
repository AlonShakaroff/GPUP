package login.admin;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.MainController;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static main.include.Constants.CLASSIC_SKIN_CSS;
import static main.include.Constants.MAIN_FXML_RESOURCE;

public class AdminLoginController {
    private Stage primaryStage;
    private MainController mainController;
    private String currentUser;

    @FXML
    public TextField userNameTextField;

    @FXML
    public Label errorMessageLabel;

    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    @FXML
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        errorMessageLabel.textProperty().bind(errorMessageProperty);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {

        String userName = userNameTextField.getText();
        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        //noinspection ConstantConditions
        String finalUrl = HttpUrl
                        .parse(Constants.LOGIN_PAGE)
                        .newBuilder()
                        .addQueryParameter("adminUsername", userName)
                        .build()
                        .toString();


        HttpClientUtil.runAsync(finalUrl,"GET",null ,new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() ->
                            errorMessageProperty.set("Login failed: " + responseBody));
                } else {
                    Platform.runLater(() -> {
                        try{
                            currentUser = userName;
                            URL url = getClass().getResource(MAIN_FXML_RESOURCE);
                            FXMLLoader fxmlLoader = new FXMLLoader();
                            fxmlLoader.setLocation(url);
                            VBox mainMenuComponent = null;
                            mainMenuComponent = fxmlLoader.load(url.openStream());
                            MainController mainController = fxmlLoader.getController();

                            Scene scene = new Scene(mainMenuComponent,1280, 800);
                            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(CLASSIC_SKIN_CSS)).toExternalForm());

                            primaryStage.hide();
                            primaryStage.setScene(scene);
                            primaryStage.centerOnScreen();
                            primaryStage.show();

                            mainController.setUserName(currentUser);
                            mainController.initialize(primaryStage);
                        }
                        catch(Exception ignore) {}
                    });
                }
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    @FXML
    private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

    public void setMainController(MainController mainController){
        this.mainController = mainController;
    }

    public void deleteCurrentUser() {
    }

    public String getCurrentUser() { return currentUser; }
}
