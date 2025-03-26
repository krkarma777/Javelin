package com.javelin;

import com.javelin.core.*;
import com.javelin.springBoot.GracefulShutdownCallback;
import com.javelin.springBoot.GracefulShutdownResult;
import com.javelin.springBoot.WebServer;
import com.javelin.springBoot.WebServerException;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Javelin's core HTTP server implementation using Java Virtual Threads.
 * <p>
 * This class is the lightweight foundation of the Javelin framework.
 * It uses Java 21 virtual threads to handle high concurrency workloads efficiently.
 * Designed to be simple, extensible, and performant for modern backend applications.
 */
public class VirtualThreadServer implements WebServer {
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadServer.class);

    private final int port;
    private final Router router = new Router();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private HttpServer server;
    private final List<Middleware> middlewares = new ArrayList<>();
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
    private void handleRequest(com.sun.net.httpserver.HttpExchange exchange) {
        HttpExchangeContext context = new HttpExchangeContext(exchange);
        context.setMiddlewareChain(middlewares);

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        JavelinHandler handler = router.findHandler(method, path);

        // Route handler or fallback 404
        context.setFinalHandler(() -> {
            if (handler != null) {
                try {
                    handler.handle(context);
                } catch (Throwable e) {
                    exceptionHandler.handle(e, context);
                }
            } else {
                handleNotFound(exchange, context);
            }
        });

        // Execute middleware chain
        try {
            context.next();
        } catch (Throwable e) {
            exceptionHandler.handle(e, context);
        }
    }

    /**
     * Sends a 404 Not Found response when no route matches.
     */
    private void handleNotFound(com.sun.net.httpserver.HttpExchange exchange, Context context) {
        try (exchange) {
            String notFound = "404 Not Found";
            exchange.sendResponseHeaders(404, notFound.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFound.getBytes());
            }
        } catch (IOException e) {
            exceptionHandler.handle(e, context);
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
     * Sets a global exception handler to handle uncaught exceptions in request processing.
     *
     * @param handler the exception handler to use
     */
    public void setExceptionHandler(ExceptionHandler handler) {
        this.exceptionHandler = handler;
    }
}
