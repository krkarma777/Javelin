package com.javelin.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.javelin.constants.HttpConstants.*;

/**
 * Javelin's routing system for mapping HTTP methods and paths to handlers.
 * <p>
 * Supports path variables via patterns like /users/{id}.
 * Internally uses a thread-safe {@link ConcurrentHashMap} for route storage.
 */
public class Router {

    /**
     * Map key = "METHOD <regex>", e.g. "GET ^/users/(?<id>[^/]+)/?$"
     * Value = Route object storing the handler & paramNames
     */
    private final Map<String, Route> routes = new ConcurrentHashMap<>();

    // ========== Public route registration ==========

    public void get(String path, JavelinHandler handler)    { addRoute(METHOD_GET, path, handler);    }
    public void post(String path, JavelinHandler handler)   { addRoute(METHOD_POST, path, handler);   }
    public void put(String path, JavelinHandler handler)    { addRoute(METHOD_PUT, path, handler);    }
    public void delete(String path, JavelinHandler handler) { addRoute(METHOD_DELETE, path, handler); }
    public void patch(String path, JavelinHandler handler)  { addRoute(METHOD_PATCH, path, handler);  }
    public void head(String path, JavelinHandler handler)   { addRoute(METHOD_HEAD, path, handler);   }

    /**
     * Finds the appropriate handler based on HTTP method and path.
     *
     * @param method      HTTP method (e.g. GET, POST)
     * @param path        the actual path requested
     * @param pathVarsOut a map to store extracted path variables
     * @return the matching handler or null if not found
     */
    public JavelinHandler findHandler(String method, String path, Map<String, String> pathVarsOut) {
        // We'll iterate over routes map entries
        for (Map.Entry<String, Route> entry : routes.entrySet()) {
            String routeKey = entry.getKey(); // e.g. "GET ^/users/(?<id>[^/]+)/?$"
            String[] keyParts = routeKey.split(" ", 2);
            if (keyParts.length < 2) continue;

            String routeMethod = keyParts[0];
            // The rest is a compiledRegex string
            String compiledRegex = keyParts[1];
            Route route = entry.getValue();

            if (!routeMethod.equalsIgnoreCase(method)) continue;

            Matcher matcher = Pattern.compile(compiledRegex).matcher(path);
            if (matcher.matches()) {
                // put extracted vars
                for (String param : route.paramNames) {
                    pathVarsOut.put(param, matcher.group(param));
                }
                return route.handler;
            }
        }
        return null;
    }

    // ========== Internal registration logic ==========

    private void addRoute(String method, String pathPattern, JavelinHandler handler) {
        // Convert pathPattern, e.g. "/users/{id}" => "^/users/(?<id>[^/]+)/?$"
        List<String> paramNames = new ArrayList<>();
        String compiledRegex = convertPathToRegex(pathPattern, paramNames);

        // store as "GET ^/users/(?<id>[^/]+)/?$"
        String routeKey = method + " " + compiledRegex;
        routes.put(routeKey, new Route(method, pathPattern, handler, paramNames));
    }

    /**
     * Converts a path pattern with {vars} into a named group regex.
     * e.g. "/users/{id}" -> "^/users/(?<id>[^/]+)/?$"
     */
    private String convertPathToRegex(String pathPattern, List<String> paramNames) {
        String[] segments = pathPattern.split("/");
        StringBuilder sb = new StringBuilder("^");

        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];
            if (seg.isEmpty()) continue;

            sb.append("/");

            if (seg.startsWith("{") && seg.endsWith("}")) {
                String varName = seg.substring(1, seg.length() - 1);
                paramNames.add(varName);
                sb.append("(?<").append(varName).append(">[^/]+)");
            } else if (seg.equals("*") && i == segments.length - 1) {
                // 마지막 segment가 *일 때만 허용
                paramNames.add("wildcard");
                sb.append("(?<wildcard>.*)");
            } else {
                sb.append(Pattern.quote(seg));
            }
        }

        sb.append("/?$");
        return sb.toString();
    }
}
