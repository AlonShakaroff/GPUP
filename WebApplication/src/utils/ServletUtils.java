package utils;


import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import target.GraphsManager;
import target.TargetGraph;
import users.UserManager;

import java.util.HashMap;

public class ServletUtils {

	private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
	private static final String GRAPHS_MANAGER_ATTRIBUTE_NAME = "graphsManager";

	private static final Object userManagerLock = new Object();
	private static final Object graphsManagerLock = new Object();

	public static UserManager getUserManager(ServletContext servletContext) {
		synchronized (userManagerLock) {
			if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
			}
		}
		return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
	}

	public static GraphsManager getGraphsManager(ServletContext servletContext) {

		synchronized (graphsManagerLock) {
			if (servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME, new GraphsManager());
			}
		}
		return (GraphsManager) servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME);
	}
}
