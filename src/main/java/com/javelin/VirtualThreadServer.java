package com.javelin;

import com.javelin.springBoot.GracefulShutdownCallback;
import com.javelin.springBoot.GracefulShutdownResult;
import com.javelin.springBoot.WebServer;
import com.javelin.springBoot.WebServerException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadServer implements WebServer {
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadServer.class);

    private final int port;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private HttpServer server;

    public VirtualThreadServer(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // 기본 "/" 경로 핸들러 등록
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) {
                    // 각 요청을 Virtual Thread에서 처리
                    executor.submit(() -> {
                        try (exchange) {
                            String response = "Hello, Virtual Thread!";
                            exchange.sendResponseHeaders(200, response.getBytes().length);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response.getBytes());
                            }
                            logger.info("Request processed successfully.");
                        } catch (IOException e) {
                            logger.error("Error processing request", e);
                        }
                    });
                }
            });

            // HttpServer에 executor 지정
            server.setExecutor(executor);
            server.start();
            logger.info("Server started on port {}", port);
        } catch (IOException e) {
            throw new WebServerException("Failed to start server", e);
        }
    }

    @Override
    public void stop() throws WebServerException {
        if (server != null) {
            server.stop(0);
            executor.shutdown();
            logger.info("Server stopped.");
        }
    }

    @Override
    public int getPort() {
        if (server != null && server.getAddress() != null) {
            return server.getAddress().getPort();
        }
        return port;
    }

    @Override
    public void shutDownGracefully(GracefulShutdownCallback callback) {
        if (server != null) {
            new Thread(() -> {
                try {
                    logger.info("Initiating graceful shutdown...");

                    // 예제에서는 1초 후에 graceful shutdown 진행
                    Thread.sleep(1000);
                    stop();

                    // shutdown 성공 시 IDLE 상태 전달
                    logger.info("Graceful shutdown completed.");
                    callback.shutdownComplete(GracefulShutdownResult.IDLE);
                } catch (InterruptedException | WebServerException e) {
                    logger.error("Error during graceful shutdown", e);
                    // 에러 발생 시 IMMEDIATE 상태 전달
                    callback.shutdownComplete(GracefulShutdownResult.IMMEDIATE);
                }
            }).start();
        }
    }

    @Override
    public void destroy() {
        if (!executor.isShutdown()) {
            executor.shutdown();
            logger.info("Executor service shut down.");
        }
    }
}
