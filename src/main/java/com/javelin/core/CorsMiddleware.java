package com.javelin.core;

import static com.javelin.constants.HttpConstants.*;

/**
 * Middleware that enables Cross-Origin Resource Sharing (CORS) support.
 * <p>
 * This middleware adds standard CORS headers to all HTTP responses,
 * allowing browsers to make cross-origin requests to the server.
 * It also handles preflight (OPTIONS) requests by returning an empty 200 OK response.
 *
 * Example:
 * <pre>
 *     server.use(new CorsMiddleware());
 * </pre>
 */
public class CorsMiddleware implements Middleware {

    /**
     * Adds CORS headers to the response and handles preflight OPTIONS requests.
     *
     * @param ctx the request/response context
     * @throws Exception if an error occurs during processing
     */
    @Override
    public void handle(Context ctx) throws Exception {
        // Set basic CORS headers
        ctx.setHeader(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        ctx.setHeader(HEADER_ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        ctx.setHeader(HEADER_ACCESS_CONTROL_ALLOW_HEADERS, HEADER_CONTENT_TYPE + ", " + HEADER_AUTHORIZATION);

        // Handle preflight (OPTIONS) requests
        if (METHOD_OPTIONS.equalsIgnoreCase(ctx.method()) ||
                ctx.header(HEADER_ACCESS_CONTROL_REQUEST_METHOD) != null) {

            ctx.status(200);
            ctx.send(""); // respond to preflight with empty body
            return;
        }

        // Proceed to the next middleware or route handler
        ctx.next();
    }
}
