package com.javelin.springBoot;

@FunctionalInterface
public interface GracefulShutdownCallback {
    void shutdownComplete(GracefulShutdownResult result);
}
