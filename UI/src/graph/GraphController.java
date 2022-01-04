package graph;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class GraphController {

    @FXML
    private TableView<?> dependenciesTableView;

    @FXML
    private TableColumn<?, ?> name;

    @FXML
    private TableColumn<?, ?> type;

    @FXML
    private TableColumn<?, ?> dependsOnDirectly;

    @FXML
    private TableColumn<?, ?> dependsOnTotal;

    @FXML
    private TableColumn<?, ?> requiredForDirectly;

    @FXML
    private TableColumn<?, ?> requiredForTotal;

    @FXML
    private TableColumn<?, ?> serialSetsAmount;

    @FXML
    private TableView<?> typeTableView;

    @FXML
    private TableColumn<?, ?> targetsAmount;

    @FXML
    private TableColumn<?, ?> independentAmount;

    @FXML
    private TableColumn<?, ?> leafAmount;

    @FXML
    private TableColumn<?, ?> middleAmount;

    @FXML
    private TableColumn<?, ?> rootAmount;

    @FXML
    private ListView<?> serialSetsListView;

}
