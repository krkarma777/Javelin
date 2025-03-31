package com.javelin.core;

import java.util.List;
import java.util.Map;

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

    /**
     * Retrieves the value of the specified HTTP request header.
     * <p>
     * Header names are case-insensitive according to the HTTP specification.
     * <p>
     * For example, {@code header("Authorization")} can retrieve an auth token.
     *
     * @param name the name of the request header
     * @return the header value if present, or {@code null} otherwise
     */
    String header(String name);

    /**
     * Passes control to the next middleware or route handler in the chain.
     * <p>
     * If no further middleware exists, the request will be routed to the matching handler.
     * <p>
     * Typically used within middleware to continue processing.
     *
     * @throws Exception if the next handler throws
     */
    void next() throws Exception;

    /**
     * Parses the request body into a Java object of the specified type.
     *
     * @param clazz the class to deserialize into
     * @return the parsed object
     * @param <T> the type of object to return
     */
    <T> T body(Class<T> clazz);

    /**
     * Sets the HTTP status code for the response.
     *
     * @param code the HTTP status code (e.g., 200, 400, 500)
     */
    void status(int code);

    /**
     * Sets a response header before the response is sent.
     *
     * @param name  the header name (e.g., "X-Custom-Header")
     * @param value the header value (e.g., "Enabled")
     */
    void setHeader(String name, String value);

    /**
     * Returns the captured path variable by name.
     *
     * @param name the variable name (e.g. "id")
     * @return the path variable's value, or null if not present
     */
    String pathVar(String name);

    /**
     * Sets all path variables captured during routing.
     */
    void setPathVars(Map<String, String> vars);

    /**
     * Sets the chain of middleware for the current request.
     *
     * @param chain the list of middleware to apply
     */
    void setMiddlewareChain(List<Middleware> chain);

    /**
     * Sets the final route handler to be invoked after all middleware.
     *
     * @param finalHandler the handler to execute at the end of the middleware chain
     */
    void setFinalHandler(Runnable finalHandler);
    
    /**
     * Sends a raw byte[] response to the client.
     * This also ends the exchange.
     *
     * @param data the raw bytes to send
     */
    void sendBytes(byte[] data);

    /**
     * Retrieves the value of a form parameter from a {@code application/x-www-form-urlencoded} request.
     * <p>
     * This method should only be used with POST or PUT requests that submit form data.
     * If the parameter does not exist or the content type is not supported, {@code null} is returned.
     *
     * @param key the form field name
     * @return the value of the form field, or {@code null} if not present
     */
    String formParam(String key);
}
