package main.include;

public class Constants {
    public final static String MAIN_FXML_RESOURCE = "/main/MainMenu.fxml";
    public final static String CONNECTIONS_FXML_RESOURCE = "/connections/Connections.fxml";
    public final static String GRAPH_FXML_RESOURCE = "/graph/Graph.fxml";
    public final static String DASHBOARD_FXML_RESOURCE = "/dashboard/AdminDashboard.fxml";
    public final static String RUNTASK_FXML_RESOURCE = "/runtask/RunTask.fxml";
    public final static String ABOUT_FXML_RESOURCE = "/aboutgpup/AboutGpup.fxml";
    public final static String LOGIN_FXML_RESOURCE = "/login/AdminLogin.fxml";

    public final static String BASE_DOMAIN = "localhost";
    public final static String APPLICATION_NAME = "/WebApplication_Web";
    private final static String FULL_SERVER_PATH = "http://" + BASE_DOMAIN + ":8080" + APPLICATION_NAME;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String USERS_LISTS = FULL_SERVER_PATH + "/userslists";
    public final static String GRAPHS_PATH = FULL_SERVER_PATH + "/graphs";
    public final static String LOGOUT_PAGE = FULL_SERVER_PATH + "/logout";
    public final static String GRAPHS_LISTS_PAGE = FULL_SERVER_PATH + "/graphslist";
    public final static String TASKS_PATH = FULL_SERVER_PATH + "/tasks";
    public final static String TASKS_LIST_PATH = FULL_SERVER_PATH + "/tasks/list";
    public final static String TASKS_OPERATION_PATH = FULL_SERVER_PATH + "/tasks/operation";
    public static final String WORKER_TASK_PAGE = FULL_SERVER_PATH + "/worker/task";
    public static final String GET_TARGETS_PAGE = FULL_SERVER_PATH + "/tasks/getTargets";


    public final static String CLASSIC_SKIN_CSS = "/main/classic.css";

    public final static String TEMP_DIR = "C:/temp";
}
