package com.javelin.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple HTTP method-based router for Javelin.
 * <p>
 * Internally uses a concurrent map with keys formatted as {@code "METHOD /path"}.
 * For example: {@code "GET /users"} or {@code "POST /login"}.
 */
public class Router {

    private final Map<String, JavelinHandler> routes = new ConcurrentHashMap<>();

    /**
     * Registers a GET route.
     *
     * @param path    the route path (e.g. {@code "/users"})
     * @param handler the handler to execute when the route is matched
     */
    public void get(String path, JavelinHandler handler) {
        routes.put("GET " + path, handler);
    }

    /**
     * Registers a POST route.
     *
     * @param path    the route path (e.g. {@code "/submit"})
     * @param handler the handler to execute when the route is matched
     */
    public void post(String path, JavelinHandler handler) {
        routes.put("POST " + path, handler);
    }

    /**
     * Looks up a registered handler for the given HTTP method and path.
     *
     * @param method the HTTP method (e.g. {@code "GET"}, {@code "POST"})
     * @param path   the request path
     * @return the matching handler, or {@code null} if no match found
     */
    public JavelinHandler findHandler(String method, String path) {
        return routes.get(method + " " + path);
    }
}
