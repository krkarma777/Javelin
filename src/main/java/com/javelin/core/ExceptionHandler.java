package com.javelin.core;

/**
 * Defines how to handle uncaught exceptions within request processing.
 */
@FunctionalInterface
public interface ExceptionHandler {

    /**
     * Handles an exception thrown during request handling.
     *
     * @param e   the thrown exception
     * @param ctx the current request context
     */
    void handle(Throwable e, Context ctx);
}
