package connections;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import main.AdminMainController;
import main.include.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import target.TargetGraph;
import task.copilation.CompilationParameters;
import task.copilation.CompilationTaskInformation;
import task.simulation.SimulationParameters;
import task.simulation.SimulationTaskInformation;
import util.http.HttpClientUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ConnectionsController {

    String taskName = "";
    String sourceTargetName;
    String destinationTargetName;
    String circleTargetName;
    String whatIfTargetName;
    private AdminMainController mainController;
    private final SimpleBooleanProperty isSourceTargetSelected;
    private final SimpleBooleanProperty isDestinationTargetSelected;
    private final SimpleBooleanProperty isWhatIfTargetSelected;
    private final SimpleBooleanProperty isCircleTargetSelected;
    private String lastVisitedDirectory = System.getProperty("user.home");
    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    private final SimpleIntegerProperty howManyTargetsSelected;
    private final SimpleIntegerProperty howManyTargetsAdded;
    private final SimpleBooleanProperty isNameEmpty;
    private final SimpleBooleanProperty isSimulationPossible;
    private final SimpleBooleanProperty isCompilationPossible;

    private final ListChangeListener<String> currentSelectedListListener;
    private final ListChangeListener<String> currentAddedListListener;

    private final ObservableList<String> TargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> filteredTargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> addedTargetsList = FXCollections.observableArrayList();

    private ObservableList<String>  currentSelectedList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedInAddedTargetsList = FXCollections.observableArrayList();

    private final SpinnerValueFactory<Double> successRateValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.50, 0.01);
    private final SpinnerValueFactory<Double> WarningRateValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.50, 0.01);
    private final SpinnerValueFactory<Integer> TimeValueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 5000);

    ObservableList<String> allTargetsNameList = FXCollections.observableArrayList();
    ObservableList<String> pathList = FXCollections.observableArrayList();
    ObservableList<String> circleList = FXCollections.observableArrayList();
    ObservableList<String> whatIfList = FXCollections.observableArrayList();
    TargetGraph targetGraph;
    Gson gson;
    SimulationParameters simulationParameters;

    public ConnectionsController() {
        howManyTargetsSelected = new SimpleIntegerProperty(0);
        currentSelectedListListener = change -> howManyTargetsSelected.set(change.getList().size());
        howManyTargetsAdded = new SimpleIntegerProperty(0);
        isNameEmpty = new SimpleBooleanProperty(true);
        isSimulationPossible = new SimpleBooleanProperty(false);
        isCompilationPossible =  new SimpleBooleanProperty(false);
        currentAddedListListener = change -> {
            howManyTargetsAdded.set(change.getList().size());
        };

        isSourceTargetSelected = new SimpleBooleanProperty(false);
        isWhatIfTargetSelected = new SimpleBooleanProperty(false);
        isDestinationTargetSelected = new SimpleBooleanProperty(false);
        isCircleTargetSelected = new SimpleBooleanProperty(false);
        this.gson = new Gson();
    }

    @FXML
    public void initialize() {
        requiredForButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        dependsOnButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        currentSelectedList = TargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedInAddedTargetsList = AddedTargetsListView.getSelectionModel().getSelectedItems();
        BooleanBinding CompilationNotOk_And_SimulationNotOk = Bindings.and(simulationTitledPane.expandedProperty().not(),
                Bindings.and(compileTaskTitledPane.expandedProperty(),
                Bindings.and(compileTaskSourceTextField.textProperty().isNotEqualTo(""),
                        compileTaskDestTextField.textProperty().isNotEqualTo(""))).not());
        BooleanBinding emptyTargets_Or_EmptyName = Bindings.or(isNameEmpty, howManyTargetsAdded.isEqualTo(0));
        addTaskButton.disableProperty().bind(Bindings.or(CompilationNotOk_And_SimulationNotOk,emptyTargets_Or_EmptyName));

        currentSelectedList.addListener(currentSelectedListListener);
        addedTargetsList.addListener(currentAddedListListener);

        addedTargetsList.clear();

        destinationComboBox.disableProperty().bind(isSourceTargetSelected.not());
        pathListView.disableProperty().bind(isDestinationTargetSelected.not());
        circleListView.disableProperty().bind(isCircleTargetSelected.not());
        whatIfListView.disableProperty().bind(isWhatIfTargetSelected.not());
        simulationTitledPane.disableProperty().bind(isSimulationPossible.not());
        compileTaskTitledPane.disableProperty().bind(isCompilationPossible.not());

        simulationSuccessRateSpinner.setValueFactory(successRateValueFactory);
        simulationWarningRateSpinner.setValueFactory(WarningRateValueFactory);
        simulationTimeSpinner.setValueFactory(TimeValueFactory);
        simulationSuccessRateSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                simulationSuccessRateSpinner.getValueFactory().setValue(0.0);
        });
        simulationWarningRateSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                simulationWarningRateSpinner.getValueFactory().setValue(0.0);
        });
        simulationTimeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                simulationTimeSpinner.getValueFactory().setValue(0);
        });
        TaskNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()){
                isNameEmpty.setValue(true);
            }
            else {
                isNameEmpty.setValue(false);
            }
            taskName = newValue;
        });

        TargetsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2)
                    addButton.fire();
            }
        });
        AddedTargetsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2)
                    removeButton.fire();
            }
        });
    }

    @FXML private TitledPane simulationTitledPane;
    @FXML private Spinner<Integer> simulationTimeSpinner;
    @FXML private CheckBox simulationRandomCheckBox;
    @FXML private Spinner<Double> simulationSuccessRateSpinner;
    @FXML private Spinner<Double> simulationWarningRateSpinner;
    @FXML private TitledPane compileTaskTitledPane;
    @FXML private TextField compileTaskSourceTextField;
    @FXML private Button compileTaskSourceSearchButton;
    @FXML private TextField compileTaskDestTextField;
    @FXML private Button compileTaskDestSearchButton;
    @FXML private TextField TaskNameTextField;
    @FXML private Button addTaskButton;
    @FXML private Color x2;
    @FXML private Font x1;
    @FXML private ListView<String> TargetsListView;
    @FXML private Button selectAllButton;
    @FXML private Button deSelectAllButton;
    @FXML private Button allTargetsButton;
    @FXML private Button requiredForButton;
    @FXML private Button dependsOnButton;
    @FXML private Button addButton;
    @FXML private Button removeButton;
    @FXML private Button clearButton;
    @FXML private Color x22;
    @FXML private Font x12;
    @FXML private ListView<String> AddedTargetsListView;
    @FXML private ComboBox<String> sourceComboBox;
    @FXML private ComboBox<String> destinationComboBox;
    @FXML private VBox pathDirectionChoiceVBox;
    @FXML private RadioButton pathRequiredForRadioButton;
    @FXML private ToggleGroup connections;
    @FXML private RadioButton pathDependsOnRadioButton;
    @FXML private ListView<String> pathListView;
    @FXML private ComboBox<String> circleTargetComboBox;
    @FXML private ListView<String> circleListView;
    @FXML private ComboBox<String> whatIfTargetComboBox;
    @FXML private VBox whatIfDirectionVBox;
    @FXML private RadioButton whatIfRequiredForRadioButton;
    @FXML private ToggleGroup conactions1;
    @FXML private RadioButton whatIfDependsOnRadioButton;
    @FXML private ListView<String> whatIfListView;

    @FXML
    void sourceComboBoxClicked(ActionEvent event) {
        sourceTargetName = sourceComboBox.getValue();
        pathList.clear();
        if(sourceTargetName != null)
            isSourceTargetSelected.set(true);
        if(isDestinationTargetSelected.get())
            refreshPathList();
    }

    @FXML
    void destinationComboBoxClicked(ActionEvent event) {
        destinationTargetName = destinationComboBox.getValue();
        pathList.clear();
        if(destinationTargetName != null) {
            isDestinationTargetSelected.set(true);
            refreshPathList();
        }
    }

    @FXML
    void pathDependsOnRadioButtonClicked(ActionEvent event) {
        if(isDestinationTargetSelected.get()) {
            refreshPathList();
        }
    }

    @FXML
    void pathRequiredForRadioButtonClicked(ActionEvent event) {
        if(isDestinationTargetSelected.get()) {
            refreshPathList();
        }
    }

    @FXML
    void pathDependsOnKeyboardPress(KeyEvent event) {
        if(isDestinationTargetSelected.get()) {
            refreshPathList();
        }
    }


    @FXML
    void pathRequiredForKeyboardPress(KeyEvent event) {
        if(isDestinationTargetSelected.get()) {
            refreshPathList();
        }
    }


    @FXML
    void whatIfTargetComboBoxClicked(ActionEvent event) {
        whatIfTargetName = whatIfTargetComboBox.getValue();
        if(whatIfTargetName != null) {
            isWhatIfTargetSelected.set(true);
            refreshWhatIfList();
        }
    }

    @FXML
    void circleTargetComboBoxClicked(ActionEvent event) {
        circleTargetName = circleTargetComboBox.getValue();
        circleList.clear();
        if(circleTargetName != null) {
            isCircleTargetSelected.set(true);
            circleList.add(targetGraph.checkIfTargetIsInACircleAndReturnCircleAsString(circleTargetName));
        }
        circleListView.setItems(circleList);
    }

    @FXML
    void whatIfDependsOnClicked(ActionEvent event) {
        if(isWhatIfTargetSelected.get())
            refreshWhatIfList();
    }

    @FXML
    void whatIfDependsOnKeyBoardPressed(KeyEvent event) {
        if(isWhatIfTargetSelected.get())
            refreshWhatIfList();
    }

    @FXML
    void whatIfRequiredForClicked(ActionEvent event) {
        if(isWhatIfTargetSelected.get())
            refreshWhatIfList();
    }

    @FXML
    void whatIfRequiredForKeyBoardPressed(KeyEvent event) {
        if(isWhatIfTargetSelected.get())
            refreshWhatIfList();
    }

    private void setAllTargetsNameList() {
        allTargetsNameList.clear();
        allTargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        sourceComboBox.setItems(allTargetsNameList.sorted());
        destinationComboBox.setItems(allTargetsNameList.sorted());
        circleTargetComboBox.setItems(allTargetsNameList.sorted());
        whatIfTargetComboBox.setItems(allTargetsNameList.sorted());

        TargetsNameList.clear();
        TargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        TargetsListView.setItems(TargetsNameList.sorted());
        TargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        AddedTargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        isSourceTargetSelected.set(false);
        isDestinationTargetSelected.set(false);
        isWhatIfTargetSelected.set(false);
        isCircleTargetSelected.set(false);
        setAllTargetsNameList();
        isSimulationPossible.setValue(targetGraph.getTaskPricing().containsKey(TargetGraph.TaskType.SIMULATION));
        isCompilationPossible.setValue(targetGraph.getTaskPricing().containsKey(TargetGraph.TaskType.COMPILATION));

        pathList.clear();
    }

    private void refreshPathList() {
        pathList.clear();
        if(pathRequiredForRadioButton.isSelected()) {
            pathList.addAll(targetGraph.getAllPathsFromTwoTargetsAsStrings(sourceTargetName,destinationTargetName, TargetGraph.pathDirection.REQUIRED_FOR));
        }
        else {
            pathList.addAll(targetGraph.getAllPathsFromTwoTargetsAsStrings(sourceTargetName,destinationTargetName, TargetGraph.pathDirection.DEPENDS_ON));
        }
        if(pathList.isEmpty())
            pathList.add("There is no path between those two targets in this direction");
        pathListView.setItems(pathList);
    }

    private void refreshWhatIfList() {
        whatIfList.clear();
        if(isWhatIfTargetSelected.get()) {
            if (whatIfRequiredForRadioButton.isSelected()) {
                whatIfList.addAll(targetGraph.getTarget(whatIfTargetName).getAllRequiredForTargetsAsStrings());
                if(whatIfList.isEmpty())
                    whatIfList.add("The target is not required for any other targets");
            }
            else {
                whatIfList.addAll(targetGraph.getTarget(whatIfTargetName).getAllDependsOnTargetsAsStrings());
                if(whatIfList.isEmpty())
                    whatIfList.add("The target does not depend on any other targets");
            }
        }
        whatIfListView.setItems(whatIfList);
    }

    //----------------------------------------------Add Task---------------------------------------//


    @FXML
    void addButtonClicked(ActionEvent event) {
        for (String str : currentSelectedList)
            if (!addedTargetsList.contains(str))
                addedTargetsList.add(str);
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void clearButtonClicked(ActionEvent event) {
        addedTargetsList.clear();
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void removeButtonClicked(ActionEvent event) {
        addedTargetsList.removeAll(currentSelectedInAddedTargetsList);
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void allTargetsButtonSelected(ActionEvent event) {
        TargetsListView.setItems(TargetsNameList);
    }

    @FXML
    void deSelectAllButtonClicked(ActionEvent event) {
        TargetsListView.getSelectionModel().clearSelection();
    }

    @FXML
    void dependsOnButtonClicked(ActionEvent event) {
        Set<String> dependsOnSet = targetGraph.getTarget(TargetsListView.getSelectionModel().
                getSelectedItem()).getAllDependsOnTargetsAsStrings();
        filteredTargetsNameList.clear();
        filteredTargetsNameList.addAll(dependsOnSet);
        TargetsListView.setItems(filteredTargetsNameList);
    }

    @FXML
    void requiredForButtonClicked(ActionEvent event) {
        Set<String> requiredForSet = targetGraph.getTarget(TargetsListView.getSelectionModel().
                getSelectedItem()).getAllRequiredForTargetsAsStrings();
        filteredTargetsNameList.clear();
        filteredTargetsNameList.addAll(requiredForSet);
        TargetsListView.setItems(filteredTargetsNameList);
    }

    @FXML
    void addTaskButtonClicked(ActionEvent event) {
        String taskName = this.TaskNameTextField.getText();
        String uploader = this.mainController.getUserName();
        String graphName = this.targetGraph.getGraphName();
        Set<String> TaskTargets = new HashSet<>();
        String taskTypeRequest = null;
        String stringObject = null;

        TaskTargets.addAll(addedTargetsList);

        if(simulationTitledPane.isExpanded())
        {
            Integer pricing = this.targetGraph.getTaskPricing().get(TargetGraph.TaskType.SIMULATION);

            SimulationParameters simulationParameters = new SimulationParameters(
                     simulationTimeSpinner.getValue(),simulationRandomCheckBox.isSelected(),
                    simulationSuccessRateSpinner.getValue(), simulationWarningRateSpinner.getValue());

            SimulationTaskInformation taskInfo = new SimulationTaskInformation
                    (taskName, uploader, graphName, TaskTargets, pricing, simulationParameters, false);

            taskTypeRequest = "Simulation";
            stringObject = this.gson.toJson(taskInfo);
        }
        else if(compileTaskTitledPane.isExpanded())
        {
            Integer pricing = this.targetGraph.getTaskPricing().get(TargetGraph.TaskType.COMPILATION);

            CompilationParameters compilationParameters = new CompilationParameters(
                    compileTaskSourceTextField.getText(),compileTaskDestTextField.getText());

            CompilationTaskInformation taskInfo = new CompilationTaskInformation(
                    taskName, uploader, graphName, TaskTargets, pricing, compilationParameters,false);

            taskTypeRequest = "Compilation";
            stringObject = this.gson.toJson(taskInfo);
        }

        uploadTaskToServer(stringObject, taskTypeRequest);
    }

    @FXML
    void selectAllButtonClicked(ActionEvent event) {
        TargetsListView.getSelectionModel().selectAll();
    }

    @FXML
    void compileTaskDestSearchButtonClicked(ActionEvent event) {
        directoryChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File dir = directoryChooser.showDialog(compileTaskDestSearchButton.getScene().getWindow());
        if (dir != null && dir.isDirectory()) {
            lastVisitedDirectory = dir.getPath();
            compileTaskDestTextField.textProperty().setValue(dir.getPath());
        }
    }

    @FXML
    void compileTaskSourceSearchButtonClicked(ActionEvent event) {
        directoryChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File dir = directoryChooser.showDialog(compileTaskSourceSearchButton.getScene().getWindow());
        if (dir != null && dir.isDirectory()) {
            lastVisitedDirectory = dir.getPath();
            compileTaskSourceTextField.textProperty().setValue(dir.getPath());
        }
    }

    @FXML
    void EnterButtonClicked(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)){
            selectAllButton.requestFocus();
        }
    }

    public void setMainController(AdminMainController mainController) {
        this.mainController = mainController;
    }

    private void uploadTaskToServer(String stringObject, String taskTypeRequest) {
        RequestBody body = RequestBody.create(stringObject, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(Constants.TASKS_PATH)
                .post(body).addHeader(taskTypeRequest, taskTypeRequest)
                .build();

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(()-> errorPopup(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if(!(response.code() >= 200 && response.code() < 300))
                    Platform.runLater(() -> errorPopup(response.header("message")));
                else // created task successfully
                   Platform.runLater(()-> mainController.setSceneToDashboardAndExpandTaskTitledPane());
            }
        });
    }

    public void errorPopup(String message) {
        Toolkit.getDefaultToolkit().beep();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Loading error");
        alert.setHeaderText(message);
        Optional<ButtonType> result = alert.showAndWait();
    }
}
