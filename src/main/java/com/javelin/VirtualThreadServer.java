package com.javelin;

import com.javelin.core.*;
import com.javelin.springBoot.GracefulShutdownCallback;
import com.javelin.springBoot.GracefulShutdownResult;
import com.javelin.springBoot.WebServer;
import com.javelin.springBoot.WebServerException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.javelin.constants.HttpConstants.HEADER_X_HTTP_METHOD_OVERRIDE;

/**
 * Javelin's core HTTP server implementation using Java Virtual Threads.
 * <p>
 * This class is the lightweight foundation of the Javelin framework.
 * It uses Java 21 virtual threads to handle high concurrency workloads efficiently.
 * Designed for simplicity, extensibility, and performance for modern backend applications.
 */
public class VirtualThreadServer implements WebServer {
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadServer.class);

    private final int port;
    private final Router router = new Router();                   // route registry
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private HttpServer server;

    // Middlewares are executed in order before the final route handler
    private final List<Middleware> middlewares = new ArrayList<>();

    // Global exception handler (default: 500 with simple message)
    private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();

    /**
     * Creates a new VirtualThreadServer instance bound to the given port.
     *
     * @param port the port to listen on (e.g. 8080)
     */
    public VirtualThreadServer(int port) {
        this.port = port;
    }

    /**
     * Starts the HTTP server, initializes context routing, and begins accepting requests.
     */
    @Override
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // All requests go through this context
            server.createContext("/", exchange ->
                    executor.submit(() ->
                            Thread.startVirtualThread(() -> handleRequest(exchange))
                    )
            );

            server.setExecutor(executor);
            server.start();
            logger.info("Server started on port {}", port);
        } catch (IOException e) {
            throw new WebServerException("Failed to start server", e);
        }
    }

    /**
     * Handles an incoming HTTP request, running middleware and the route handler.
     *
     * @param exchange the raw HTTP exchange from com.sun.net.httpserver
     */
    private void handleRequest(HttpExchange exchange) {
        // Create context for this request
        HttpExchangeContext context = new HttpExchangeContext(exchange);
        context.setMiddlewareChain(middlewares);

        // Possibly override method (PATCH, etc.)
        String method = exchange.getRequestMethod();
        String override = exchange.getRequestHeaders().getFirst(HEADER_X_HTTP_METHOD_OVERRIDE);
        if (override != null && !override.isBlank()) {
            method = override.toUpperCase();
        }

        String path = exchange.getRequestURI().getPath();

        // For path variables
        Map<String, String> pathVars = new HashMap<>();
        JavelinHandler handler = router.findHandler(method, path, pathVars);

        // Set extracted variables into the context
        context.setPathVars(pathVars);

        // Final route or fallback 404
        context.setFinalHandler(() -> {
            if (handler != null) {
                try {
                    handler.handle(context);
                } catch (Throwable e) {
                    exceptionHandler.handle(e, context);
                }
            } else {
                respondNotFound(exchange, context);
            }
        });

        // Run middleware chain → final handler
        try {
            context.next();
        } catch (Throwable e) {
            exceptionHandler.handle(e, context);
        }
    }

    /**
     * Sends a 404 Not Found response when no route matches.
     */
    private void respondNotFound(HttpExchange exchange, Context ctx) {
        try (exchange) {
            String notFound = "404 Not Found";
            exchange.sendResponseHeaders(404, notFound.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFound.getBytes());
            }
        } catch (IOException e) {
            exceptionHandler.handle(e, ctx);
        }
    }

    /**
     * Stops the server and releases all resources.
     */
    @Override
    public void stop() throws WebServerException {
        if (server != null) {
            server.stop(0);
            executor.shutdown();
            logger.info("Server stopped.");
        }
    }

    /**
     * Returns the actual port the server is bound to.
     *
     * @return the bound port
     */
    @Override
    public int getPort() {
        if (server != null && server.getAddress() != null) {
            return server.getAddress().getPort();
        }
        return port;
    }

    /**
     * Performs a graceful shutdown with callback once completed.
     *
     * @param callback the shutdown callback to invoke when done
     */
    @Override
    public void shutDownGracefully(GracefulShutdownCallback callback) {
        if (server != null) {
            new Thread(() -> {
                try {
                    logger.info("Initiating graceful shutdown...");
                    Thread.sleep(1000); // let in-flight requests complete
                    stop();
                    callback.shutdownComplete(GracefulShutdownResult.IDLE);
                    logger.info("Graceful shutdown completed.");
                } catch (InterruptedException | WebServerException e) {
                    logger.error("Error during graceful shutdown", e);
                    callback.shutdownComplete(GracefulShutdownResult.IMMEDIATE);
                }
            }).start();
        }
    }

    /**
     * Shuts down internal executor service (usually called from destroy hooks).
     */
    @Override
    public void destroy() {
        if (!executor.isShutdown()) {
            executor.shutdown();
            logger.info("Executor service shut down.");
        }
    }

    /**
     * Registers a middleware function to be executed for all requests.
     *
     * @param middleware the middleware function
     */
    public void use(Middleware middleware) {
        middlewares.add(middleware);
    }

    // ============= Route registration methods =============

    /**
     * Registers a GET route with its handler.
     *
     * @param path    the path to match (e.g. "/users")
     * @param handler the handler to execute
     */
    public void get(String path, JavelinHandler handler) {
        router.get(path, handler);
    }

    /**
     * Registers a POST route with its handler.
     *
     * @param path    the path to match (e.g. "/submit")
     * @param handler the handler to execute
     */
    public void post(String path, JavelinHandler handler) {
        router.post(path, handler);
    }

    /**
     * Registers a PUT route.
     *
     * @param path    the request path (e.g. {@code "/update"})
     * @param handler the handler to execute
     */
    public void put(String path, JavelinHandler handler) {
        router.put(path, handler);
    }

    /**
     * Registers a DELETE route.
     *
     * @param path    the request path (e.g. {@code "/delete"})
     * @param handler the handler to execute
     */
    public void delete(String path, JavelinHandler handler) {
        router.delete(path, handler);
    }

    /**
     * Registers a PATCH route.
     *
     * @param path    the request path (e.g. {@code "/modify"})
     * @param handler the handler to execute
     */
    public void patch(String path, JavelinHandler handler) {
        router.patch(path, handler);
    }

    /**
     * Registers a HEAD route.
     *
     * @param path    the request path (e.g. {@code "/ping"})
     * @param handler the handler to execute
     */
    public void head(String path, JavelinHandler handler) {
        router.head(path, handler);
    }

    /**
     * Sets a global exception handler to handle uncaught exceptions in request processing.
     *
     * @param handler the exception handler to use
     */
    public void setExceptionHandler(ExceptionHandler handler) {
        this.exceptionHandler = handler;
    }
}
