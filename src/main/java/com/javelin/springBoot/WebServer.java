package com.javelin.springBoot;

public interface WebServer {
    void start() throws WebServerException;

    void stop() throws WebServerException;

    int getPort();

    default void shutDownGracefully(GracefulShutdownCallback callback) {
        callback.shutdownComplete(GracefulShutdownResult.IMMEDIATE);
    }

    default void destroy() {
        this.stop();
    }
}
