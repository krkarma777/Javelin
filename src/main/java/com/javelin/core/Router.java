package com.javelin.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.javelin.constants.HttpConstants.*;

/**
 * Javelin's routing system for mapping HTTP methods and paths to handlers.
 * <p>
 * Internally uses a thread-safe {@link ConcurrentHashMap} to register and lookup routes.
 */
public class Router {

    private final Map<String, JavelinHandler> routes = new ConcurrentHashMap<>();

    /**
     * Registers a GET route.
     *
     * @param path    the route path (e.g. "/users")
     * @param handler the handler to execute for this route
     */
    public void get(String path, JavelinHandler handler) {
        addRoute(METHOD_GET, path, handler);
    }

    /**
     * Registers a POST route.
     */
    public void post(String path, JavelinHandler handler) {
        addRoute(METHOD_POST, path, handler);
    }

    /**
     * Registers a PUT route.
     */
    public void put(String path, JavelinHandler handler) {
        addRoute(METHOD_PUT, path, handler);
    }

    /**
     * Registers a DELETE route.
     */
    public void delete(String path, JavelinHandler handler) {
        addRoute(METHOD_DELETE, path, handler);
    }

    /**
     * Registers a PATCH route.
     */
    public void patch(String path, JavelinHandler handler) {
        addRoute(METHOD_PATCH, path, handler);
    }

    /**
     * Registers a HEAD route.
     */
    public void head(String path, JavelinHandler handler) {
        addRoute(METHOD_HEAD, path, handler);
    }

    /**
     * Finds the appropriate handler based on HTTP method and path.
     *
     * @param method      HTTP method (e.g. GET, POST)
     * @param path        the path requested
     * @param pathVarsOut to hold the extracted path variables
     * @return the matching handler or null if not found
     */
    public JavelinHandler findHandler(String method, String path, Map<String, String> pathVarsOut) {
        for (Map.Entry<String, JavelinHandler> entry : routes.entrySet()) {
            String routeKey = entry.getKey();
            String[] parts = routeKey.split(" ", 2); // "GET /path" -> ["GET", "/path"]
            if (parts.length != 2) continue;

            String routeMethod = parts[0];
            String routePath = parts[1];

            if (!routeMethod.equalsIgnoreCase(method)) continue;

            // 경로에서 변수 이름을 추출하고, 해당 변수값을 pathVarsOut에 추가
            Matcher matcher = Pattern.compile(routePath).matcher(path);
            if (matcher.matches()) {
                List<String> paramNames = getParamNamesFromPath(routePath);
                for (String name : paramNames) {
                    pathVarsOut.put(name, matcher.group(name));
                }
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Registers a route with a handler (e.g., GET /users).
     */
    private void addRoute(String method, String pathPattern, JavelinHandler handler) {
        // routeKey = "GET /users/{id}" 형식으로 경로와 메서드를 조합하여 Map에 저장
        String routeKey = method + " " + pathPattern;
        routes.put(routeKey, handler);
    }

    // Helper method to extract parameter names from path (e.g., "/users/{id}" -> ["id"])
    private List<String> getParamNamesFromPath(String path) {
        List<String> paramNames = new ArrayList<>();
        String[] segments = path.split("/");
        for (String segment : segments) {
            if (segment.startsWith("{") && segment.endsWith("}")) {
                paramNames.add(segment.substring(1, segment.length() - 1));
            }
        }
        return paramNames;
    }
}
