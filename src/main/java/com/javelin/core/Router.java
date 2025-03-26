package com.javelin.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Router {
    private final Map<String, JavelinHandler> routes = new ConcurrentHashMap<>();

    public void get(String path, JavelinHandler handler) {
        routes.put("GET " + path, handler);
    }

    public void post(String path, JavelinHandler handler) {
        routes.put("POST " + path, handler);
    }

    public JavelinHandler findHandler(String method, String path) {
        return routes.get(method + " " + path);
    }
}
