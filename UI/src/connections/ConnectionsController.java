package connections;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
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
