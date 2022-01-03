package connections;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;

public class ConnectionsController {

    @FXML
    private ComboBox<?> sourceComboBox;

    @FXML
    private ComboBox<?> destinationComboBox;

    @FXML
    private RadioButton pathReqForradioButton;

    @FXML
    private ToggleGroup conactions;

    @FXML
    private RadioButton pathDependsOnRadioButton;

    @FXML
    private Button pathSubmitButton;

    @FXML
    private ListView<?> pathListView;

    @FXML
    private ComboBox<?> circleTargetComboBox;

    @FXML
    private Button circleSubmitButton;

    @FXML
    private ListView<?> circleListView;

    @FXML
    private ComboBox<?> whatIfTargetComboBox;

    @FXML
    private RadioButton whatIfReqForRadioButton;

    @FXML
    private ToggleGroup conactions1;

    @FXML
    private RadioButton whatIfDependsOnRadioButton;

    @FXML
    private Button WhatIfSubmitButton;

    @FXML
    private TableView<?> WhatIfTable;

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

}
