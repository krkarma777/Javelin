package com.javelin.core;

/**
 * Functional interface for handling HTTP requests in Javelin.
 * <p>
 * Each route is mapped to a {@code JavelinHandler}, which is executed
 * with the associated {@link Context} object.
 * <p>
 * Example usage:
 * <pre>{@code
 * server.get("/hello", ctx -> ctx.send("Hello, world!"));
 * }</pre>
 */
@FunctionalInterface
public interface JavelinHandler {

    /**
     * Handles the incoming HTTP request.
     *
     * @param ctx the request/response context
     * @throws Exception optional exception during handling (e.g. serialization errors)
     */
    void handle(Context ctx) throws Exception;
}
