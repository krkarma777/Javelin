package com.javelin.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.javelin.constants.HttpConstants.*;

/**
 * Implementation of {@link Context} based on Java's built-in {@link HttpExchange}.
 * <p>
 * This class wraps the low-level {@code HttpExchange} object and provides
 * utility methods to simplify request/response handling for Javelin handlers.
 */
public class HttpExchangeContext implements Context {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpExchange exchange;
    private final Map<String, String> queryParams;

    // Path variables extracted from router
    private final Map<String, String> pathVars = new HashMap<>();

    // Middleware chain
    private List<Middleware> middlewareChain;
    private Runnable finalHandler;
    private int currentIndex = -1;

    // Response status code
    private int statusCode = 200;

    /**
     * Constructs a new context based on the provided {@code HttpExchange}.
     *
     * @param exchange the underlying HTTP exchange
     */
    public HttpExchangeContext(HttpExchange exchange) {
        this.exchange = exchange;
        this.queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
    }

    // ========== Path & Query ==========

    /**
     * Returns the path portion of the request URI.
     *
     * @return the path (e.g. {@code "/users"})
     */
    @Override
    public String path() {
        return exchange.getRequestURI().getPath();
    }

    /**
     * Returns the value of a query parameter from the request URL.
     * <p>
     * For example, given "/search?q=hello", calling {@code queryParam("q")} returns {@code "hello"}.
     *
     * @param key the name of the query parameter
     * @return the value, or {@code null} if not found
     */
    @Override
    public String queryParam(String key) {
        return queryParams.get(key);
    }

    /**
     * Returns the captured path variable by name.
     * E.g., for "/users/{id}" if path is "/users/123", then {@code pathVar("id")} => "123".
     *
     * @param name the variable name (e.g. "id")
     * @return the path variable's value, or null if not present
     */
    @Override
    public String pathVar(String name) {
        return pathVars.get(name);
    }

    /**
     * Sets all path variables captured during routing.
     *
     * @param vars a map of variableName -> value
     */
    @Override
    public void setPathVars(Map<String, String> vars) {
        this.pathVars.putAll(vars);
    }

    // ========== Response Handling ==========

    /**
     * Sets the HTTP status code for the response.
     * This must be called before sending the response.
     *
     * @param code the HTTP status code (e.g., 200, 404, 500)
     */
    @Override
    public void status(int code) {
        this.statusCode = code;
    }

    /**
     * Sets a response header before sending the response.
     *
     * @param name  the header name (e.g. "X-Custom-Header")
     * @param value the header value (e.g. "Enabled")
     */
    @Override
    public void setHeader(String name, String value) {
        exchange.getResponseHeaders().set(name, value);
    }

    /**
     * Sends a plain text response with the currently set HTTP status.
     * This also closes the exchange.
     *
     * @param body the response body as plain text
     */
    @Override
    public void send(String body) {
        try {
            String method = exchange.getRequestMethod();
            byte[] bytes = body.getBytes();

            // HEAD request => no body
            if (METHOD_HEAD.equalsIgnoreCase(method)) {
                exchange.getResponseHeaders().set(HEADER_CONTENT_LENGTH, "0");
                exchange.sendResponseHeaders(statusCode, 0);
            } else {
                exchange.sendResponseHeaders(statusCode, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    /**
     * Sends a JSON response with the currently set HTTP status.
     * Sets {@code Content-Type: application/json}.
     * This also closes the exchange.
     *
     * @param data the object to serialize into JSON
     */
    @Override
    public void json(Object data) {
        try {
            byte[] json = mapper.writeValueAsBytes(data);
            exchange.getResponseHeaders().set(HEADER_CONTENT_TYPE, APPLICATION_JSON);
            exchange.sendResponseHeaders(statusCode, json.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    /**
     * Returns the value of a request header.
     *
     * @param name header name (case-insensitive)
     * @return the header value or {@code null} if not present
     */
    @Override
    public String header(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    // ========== Request Body ==========

    /**
     * Parses the HTTP request body as JSON into the given class type.
     *
     * @param clazz the class to deserialize into
     * @param <T>   the type of object to return
     * @return the deserialized object
     * @throws RuntimeException if the body cannot be parsed
     */
    @Override
    public <T> T body(Class<T> clazz) {
        try (InputStream is = exchange.getRequestBody()) {
            return mapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse request body", e);
        }
    }

    // ========== Middleware Chain ==========

    @Override
    public void setMiddlewareChain(List<Middleware> chain) {
        this.middlewareChain = chain;
    }

    @Override
    public void setFinalHandler(Runnable finalHandler) {
        this.finalHandler = finalHandler;
    }

    /**
     * Proceeds to the next middleware in the chain, or the final route handler.
     *
     * @throws Exception if any middleware or the final handler throws
     */
    @Override
    public void next() throws Exception {
        currentIndex++;
        if (middlewareChain != null && currentIndex < middlewareChain.size()) {
            middlewareChain.get(currentIndex).handle(this);
        } else if (finalHandler != null) {
            finalHandler.run();
        }
    }

    // ========== Internal Helpers ==========

    /**
     * Parses query parameters from the raw query string (e.g. "id=1&name=abc").
     *
     * @param rawQuery the raw query (may be null)
     * @return a map of parameter names and values
     */
    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> result = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return result;

        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String name = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            result.put(name, value);
        }
        return result;
    }

    /**
     * Safely decodes a URL-encoded string using UTF-8.
     *
     * @param value the encoded string
     * @return the decoded string
     */
    private String decode(String value) {
        try {
            return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
