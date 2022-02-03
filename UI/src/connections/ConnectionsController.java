package connections;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import target.Target;
import target.TargetGraph;

import javax.swing.event.ChangeEvent;
import java.util.HashSet;
import java.util.Set;

public class ConnectionsController {
    String sourceTargetName;
    String destinationTargetName;
    String circleTargetName;
    String whatIfTargetName;
    private SimpleBooleanProperty isSourceTargetSelected;
    private SimpleBooleanProperty isDestinationTargetSelected;
    private SimpleBooleanProperty isWhatIfTargetSelected;
    private SimpleBooleanProperty isCircleTargetSelected;

    ObservableList<String> allTargetsNameList = FXCollections.observableArrayList();
    ObservableList<String> pathList = FXCollections.observableArrayList();
    ObservableList<String> circleList = FXCollections.observableArrayList();
    ObservableList<String> whatIfList = FXCollections.observableArrayList();
    TargetGraph targetGraph;

    public ConnectionsController() {
       isSourceTargetSelected = new SimpleBooleanProperty(false);
       isWhatIfTargetSelected = new SimpleBooleanProperty(false);
       isDestinationTargetSelected = new SimpleBooleanProperty(false);
       isCircleTargetSelected = new SimpleBooleanProperty(false);
    }

    @FXML
    public void initialize() {
       destinationComboBox.disableProperty().bind(isSourceTargetSelected.not());
       pathListView.disableProperty().bind(isDestinationTargetSelected.not());
       circleListView.disableProperty().bind(isCircleTargetSelected.not());
       whatIfListView.disableProperty().bind(isWhatIfTargetSelected.not());
    }
    
    @FXML
    private TitledPane simulationTitledPane;

    @FXML
    private Spinner<Integer> simulationTimeSpinner;

    @FXML
    private CheckBox simulationRandomCheckBox;

    @FXML
    private Spinner<Double> simulationSuccessRateSpinner;

    @FXML
    private Spinner<Double> simulationWarningRateSpinner;

    @FXML
    private TitledPane compileTaskTitledPane;

    @FXML
    private TextField compileTaskSourceTextField;

    @FXML
    private Button compileTaskSourceSearchButton;

    @FXML
    private TextField compileTaskDestTextField;

    @FXML
    private Button compileTaskDestSearchButton;

    @FXML
    private TextField TaskNameTextField;

    @FXML
    private Button addTaskButton;

    @FXML
    private Color x2;

    @FXML
    private Font x1;

    @FXML
    private ListView<String> TargetsListView;

    @FXML
    private Button selectAllButton;

    @FXML
    private Button deSelectAllButton;

    @FXML
    private Button allTargetsButton;

    @FXML
    private Button requiredForButton;

    @FXML
    private Button dependsOnButton;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button clearButton;

    @FXML
    private Color x22;

    @FXML
    private Font x12;

    @FXML
    private ListView<String> AddedTargetsListView;


    @FXML
    void addButtonClicked(ActionEvent event) {

    }

    @FXML
    void addTaskButtonClicked(ActionEvent event) {

    }

    @FXML
    void allTargetsButtonSelected(ActionEvent event) {

    }

    @FXML
    void clearButtonClicked(ActionEvent event) {

    }

    @FXML
    void compileTaskDestSearchButtonClicked(ActionEvent event) {

    }

    @FXML
    void compileTaskSourceSearchButtonClicked(ActionEvent event) {

    }

    @FXML
    void deSelectAllButtonClicked(ActionEvent event) {

    }

    @FXML
    void dependsOnButtonClicked(ActionEvent event) {

    }

    @FXML
    void removeButtonClicked(ActionEvent event) {

    }

    @FXML
    void requiredForButtonClicked(ActionEvent event) {

    }

    @FXML
    void selectAllButtonClicked(ActionEvent event) {

    }

    @FXML
    private ComboBox<String> sourceComboBox;

    @FXML
    private ComboBox<String> destinationComboBox;

    @FXML
    private VBox pathDirectionChoiceVBox;

    @FXML
    private RadioButton pathRequiredForRadioButton;

    @FXML
    private ToggleGroup connections;

    @FXML
    private RadioButton pathDependsOnRadioButton;

    @FXML
    private Button pathSubmitButton;

    @FXML
    private ListView<String> pathListView;

    @FXML
    private ComboBox<String> circleTargetComboBox;

    @FXML
    private Button circleSubmitButton;

    @FXML
    private ListView<String> circleListView;

    @FXML
    private ComboBox<String> whatIfTargetComboBox;

    @FXML
    private VBox whatIfDirectionVBox;

    @FXML
    private RadioButton whatIfRequiredForRadioButton;

    @FXML
    private RadioButton whatIfDependsOnRadioButton;

    @FXML
    private Button WhatIfSubmitButton;

    @FXML
    private ListView<String> whatIfListView;

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
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        isSourceTargetSelected.set(false);
        isDestinationTargetSelected.set(false);
        isWhatIfTargetSelected.set(false);
        isCircleTargetSelected.set(false);
        setAllTargetsNameList();
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
}
