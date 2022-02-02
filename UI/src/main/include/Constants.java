package main.include;

public class Constants {
    public final static String MAIN_FXML_RESOURCE = "/main/MainMenu.fxml";
    public final static String CONNECTIONS_FXML_RESOURCE = "/connections/Connections.fxml";
    public final static String GRAPH_FXML_RESOURCE = "/graph/Graph.fxml";
    public final static String RUNTASK_FXML_RESOURCE = "/runtask/RunTask.fxml";
    public final static String ABOUT_FXML_RESOURCE = "/aboutgpup/AboutGpup.fxml";
    public final static String LOGIN_FXML_RESOURCE = "/login/login.fxml";

    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/WebApplication_Web_exploded";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";

    public final static String CLASSIC_SKIN_CSS = "/main/classic.css";
}
