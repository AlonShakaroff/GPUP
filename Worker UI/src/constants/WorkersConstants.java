package constants;

public class WorkersConstants {
    /*------------------------------------------FXMLs-------------------------------------------------------*/
    public static final String WORKERS_LOGIN_FXML_RESOURCE = "/login/WorkersLogin.fxml";
    public static final String WORKERS_DASHBOARD_FXML_RESOURCE = "/dashboard/WorkersDashboard.fxml";
    public static final String WORKERS_MAIN_MENU_FXML_RESOURCE = "/main/WorkersMainMenu.fxml";
    public static final String WORKERS_TASKS_CONTROL_FXML_RESOURCE = "/tasks/control/WorkersTasksControl.fxml";
    public static final String ABOUT_FXML_RESOURCE = "/aboutgpup/AboutGpup.fxml";


    /*---------------------------------------Images & CSS---------------------------------------------------*/
    public static final String ICON_IMAGE = "/resources/images/icon.png";
    public static final String CLASSIC_CSS = "/main/css/classic.css";
    public static final String CHALKBOARD_CSS = "/main/css/chalkBoard.css";
    public static final String STREET_CSS = "/main/css/street.css";
    public static final String AVIAD_CSS = "/main/css/aviad.css";

    /*--------------------------------------------Servlets---------------------------------------------------*/
    public final static String BASE_DOMAIN = "localhost";
    private final static String FULL_SERVER_PATH = "http://" + BASE_DOMAIN + ":8080";

    public static final String WORKER_TASK_PAGE = FULL_SERVER_PATH + "/worker/task";
}
