 package connections;

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
import javafx.scene.layout.VBox;
import target.TargetGraph;

 public class ConnectionsController {

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
    private TableView<String> WhatIfTable;

    @FXML
    void WhatIfSubmitButtonClicked(ActionEvent event) {

    }

    @FXML
    void circleSubmitButtonClicked(ActionEvent event) {

    }

    @FXML
    void circleTargetComboBoxClicked(ActionEvent event) {

    }

    @FXML
    void destinationComboBoxClicked(ActionEvent event) {

    }

    @FXML
    void pathSubmitButtonClicked(ActionEvent event) {

    }

    @FXML
    void sourceComboBoxClicked(ActionEvent event) {

    }

    @FXML
    void whatIfTargetComboBoxClicked(ActionEvent event) {

    }

    ObservableList<String> serialSetNameList = FXCollections.observableArrayList();
    ObservableList<String> allTargetsNameList = FXCollections.observableArrayList();

    TargetGraph targetGraph;

//    void setTargetGraph(TargetGraph targetGraph) {
//        this.targetGraph = targetGraph;
//        if()
//    }

}
