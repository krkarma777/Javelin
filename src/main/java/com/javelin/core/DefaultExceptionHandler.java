package com.javelin.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Default exception handler used by Javelin when no user-defined handler is set.
 * <p>
 * Provides consistent HTTP status codes based on common exception types,
 * and logs unexpected server errors for diagnostics.
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public void handle(Throwable e, Context ctx) {
        int status;
        String errorMsg;

        // Map specific exception types to status codes
        if (e instanceof IllegalArgumentException) {
            status = 400;
            errorMsg = "Bad Request: " + e.getMessage();
        } else if (e instanceof SecurityException) {
            status = 403;
            errorMsg = "Forbidden: " + e.getMessage();
        } else {
            status = 500;
            errorMsg = "Internal Server Error";
            log.error("Unhandled exception during request processing", e);
        }

        // Respond with JSON
        ctx.status(status);
        ctx.json(Map.of(
                "status", status,
                "error", errorMsg
        ));
    }
}
