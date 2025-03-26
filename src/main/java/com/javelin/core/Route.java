package com.javelin.core;

import java.util.List;

/**
 * Holds the raw info about a route:
 * - original method (GET, POST, etc.)
 * - original path pattern (e.g. /users/{id})
 * - the handler
 * - the paramNames extracted
 *
 * The compiled regex is stored as a string key in the Map in Router.
 */
public class Route {
    public final String method;
    public final String originalPath;
    public final JavelinHandler handler;
    public final List<String> paramNames;

    public Route(String method, String originalPath, JavelinHandler handler, List<String> paramNames) {
        this.method = method;
        this.originalPath = originalPath;
        this.handler = handler;
        this.paramNames = paramNames;
    }
}
