package com.javelin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javelin.core.Context;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link Context} based on Java's built-in {@link HttpExchange}.
 * <p>
 * This class wraps the low-level {@code HttpExchange} object and provides
 * utility methods to simplify request/response handling for Javelin handlers.
 */
public class HttpExchangeContext implements Context {

    private final HttpExchange exchange;
    private final Map<String, String> queryParams;
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs a new context based on the provided {@code HttpExchange}.
     *
     * @param exchange the underlying HTTP exchange
     */
    public HttpExchangeContext(HttpExchange exchange) {
        this.exchange = exchange;
        this.queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
    }

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
     * Sends a plain text response with status 200.
     * This also closes the exchange.
     *
     * @param body the response body as plain text
     */
    @Override
    public void send(String body) {
        try {
            byte[] bytes = body.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    /**
     * Retrieves the value of a query parameter from the request URL.
     *
     * @param key the name of the query parameter
     * @return the value, or {@code null} if not found
     */
    @Override
    public String queryParam(String key) {
        return queryParams.get(key);
    }

    /**
     * Sends a JSON response with status 200.
     * Sets {@code Content-Type: application/json}.
     * This also closes the exchange.
     *
     * @param data the object to serialize into JSON
     */
    @Override
    public void json(Object data) {
        try {
            byte[] json = mapper.writeValueAsBytes(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length);
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
     * @return the header value or null if not present
     */
    @Override
    public String header(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    /**
     * Parses query parameters from the raw query string.
     *
     * @param rawQuery the raw query string (e.g. {@code "id=1&name=abc"})
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
