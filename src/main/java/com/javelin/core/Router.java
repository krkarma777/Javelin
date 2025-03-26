package com.javelin.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        routes.put("GET " + path, handler);
    }

    /**
     * Registers a POST route.
     */
    public void post(String path, JavelinHandler handler) {
        routes.put("POST " + path, handler);
    }

    /**
     * Registers a PUT route.
     */
    public void put(String path, JavelinHandler handler) {
        routes.put("PUT " + path, handler);
    }

    /**
     * Registers a DELETE route.
     */
    public void delete(String path, JavelinHandler handler) {
        routes.put("DELETE " + path, handler);
    }

    /**
     * Registers a PATCH route.
     */
    public void patch(String path, JavelinHandler handler) {
        routes.put("PATCH " + path, handler);
    }

    /**
     * Registers a HEAD route.
     */
    public void head(String path, JavelinHandler handler) {
        routes.put("HEAD " + path, handler);
    }

    /**
     * Finds the appropriate handler based on HTTP method and path.
     *
     * @param method HTTP method (e.g. GET, POST)
     * @param path   the path requested
     * @return the matching handler or null if not found
     */
    public JavelinHandler findHandler(String method, String path) {
        return routes.get(method + " " + path);
    }
}
