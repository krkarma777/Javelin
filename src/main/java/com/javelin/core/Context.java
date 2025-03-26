package com.javelin.core;

/**
 * Represents a lightweight abstraction over an HTTP request and response.
 * <p>
 * This interface is provided to handler methods in Javelin and allows
 * easy access to request data and response construction.
 */
public interface Context {

    /**
     * Returns the path of the HTTP request.
     *
     * @return the request URI path (e.g., "/users", "/api/items")
     */
    String path();

    /**
     * Sends a plain text response to the client.
     * This ends the exchange.
     *
     * @param body the response body to send as plain text
     */
    void send(String body);

    /**
     * Returns the value of a query parameter from the request URL.
     * <p>
     * For example, given "/search?q=hello", calling {@code queryParam("q")} returns {@code "hello"}.
     *
     * @param key the name of the query parameter
     * @return the value of the parameter, or {@code null} if not present
     */
    String queryParam(String key);

    /**
     * Sends a JSON response to the client.
     * The object will be serialized using the default ObjectMapper.
     * This ends the exchange.
     *
     * @param data the object to serialize as JSON
     */
    void json(Object data);
}
