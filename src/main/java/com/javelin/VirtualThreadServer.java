package com.javelin;

import com.javelin.core.JavelinHandler;
import com.javelin.core.Router;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Javelin's core HTTP server implementation using Java Virtual Threads.
 * <p>
 * Provides a minimal, high-concurrency alternative to traditional WAS systems
 * like Tomcat or Jetty.
 */
public class VirtualThreadServer implements WebServer {
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadServer.class);

    private final int port;
    private final Router router = new Router();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Virtual Thread 기반 실행기
    private HttpServer server;

    /**
     * Creates a new VirtualThreadServer listening on the given port.
     *
     * @param port the port to bind the HTTP server to
     */
    public VirtualThreadServer(int port) {
        this.port = port;
    }

    /**
     * Starts the HTTP server and sets up the routing logic.
     */
    @Override
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/", exchange -> {
                executor.submit(() -> {
                    Thread.startVirtualThread(() -> {
                        String method = exchange.getRequestMethod();
                        String path = exchange.getRequestURI().getPath();

                        JavelinHandler handler = router.findHandler(method, path);

                        if (handler != null) {
                            try {
                                handler.handle(new HttpExchangeContext(exchange));
                            } catch (Exception e) {
                                e.printStackTrace(); // TODO: Custom error handler
                            }
                        } else {
                            // 404 처리
                            try {
                                String notFound = "404 Not Found";
                                exchange.sendResponseHeaders(404, notFound.length());
                                try (OutputStream os = exchange.getResponseBody()) {
                                    os.write(notFound.getBytes());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                exchange.close();
                            }
                        }
                    });
                });
            });

            server.setExecutor(executor);
            server.start();
            logger.info("Server started on port {}", port);
        } catch (IOException e) {
            throw new WebServerException("Failed to start server", e);
        }
    }

    /**
     * Stops the server immediately.
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
     * Returns the actual bound port, useful when using port 0 (auto-assign).
     */
    @Override
    public int getPort() {
        if (server != null && server.getAddress() != null) {
            return server.getAddress().getPort();
        }
        return port;
    }

    /**
     * Gracefully shuts down the server with callback support.
     */
    @Override
    public void shutDownGracefully(GracefulShutdownCallback callback) {
        if (server != null) {
            new Thread(() -> {
                try {
                    logger.info("Initiating graceful shutdown...");
                    Thread.sleep(1000); // Give in-flight requests time to finish
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
     * Shuts down the executor service if not already stopped.
     */
    @Override
    public void destroy() {
        if (!executor.isShutdown()) {
            executor.shutdown();
            logger.info("Executor service shut down.");
        }
    }

    /**
     * Registers a GET route.
     *
     * @param path    the request path (e.g. {@code "/hello"})
     * @param handler the handler to execute
     */
    public void get(String path, JavelinHandler handler) {
        router.get(path, handler);
    }

    /**
     * Registers a POST route.
     *
     * @param path    the request path (e.g. {@code "/submit"})
     * @param handler the handler to execute
     */
    public void post(String path, JavelinHandler handler) {
        router.post(path, handler);
    }
}
