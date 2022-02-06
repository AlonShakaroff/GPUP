package login;

import constants.WorkersConstants;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.WorkerMainController;
import main.include.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;


public class WorkerLoginController {
    private Stage primaryStage;
    private WorkerMainController workerMainController;
    private String currentUser = null;

    @FXML public TextField userNameTextField;
    @FXML private Spinner<Integer> ThreadAmountSpinner;
    @FXML public Label errorMessageLabel;

    private final StringProperty errorMessageProperty = new SimpleStringProperty();
    private final SpinnerValueFactory<Integer> ThreadsAmountValueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1);

    @FXML
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        errorMessageLabel.textProperty().bind(errorMessageProperty);
        ThreadAmountSpinner.setEditable(false);
        ThreadAmountSpinner.setValueFactory(ThreadsAmountValueFactory);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {
        String userName = userNameTextField.getText();
        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("workerUsername", userName)
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
                            URL url = getClass().getResource(WorkersConstants.WORKERS_MAIN_MENU_FXML_RESOURCE);
                            FXMLLoader fxmlLoader = new FXMLLoader();
                            fxmlLoader.setLocation(url);
                            VBox mainMenuComponent = null;
                            mainMenuComponent = fxmlLoader.load(url.openStream());
                            WorkerMainController workerMainController = fxmlLoader.getController();

                            Scene scene = new Scene(mainMenuComponent,1280, 800);
                            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(WorkersConstants.CLASSIC_CSS)).toExternalForm());

                            primaryStage.hide();
                            primaryStage.setScene(scene);
                            primaryStage.centerOnScreen();
                            primaryStage.show();

                            workerMainController.setUserName(currentUser);
                            workerMainController.initialize(primaryStage);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
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

    public void setMainController(WorkerMainController workerMainController){
        this.workerMainController = workerMainController;
    }

    public void deleteCurrentUser() {
    }

    public String getCurrentUser() { return currentUser; }
}
