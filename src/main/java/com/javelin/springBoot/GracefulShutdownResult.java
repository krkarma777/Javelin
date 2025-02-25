package com.javelin.springBoot;

public enum GracefulShutdownResult {
    REQUESTS_ACTIVE,
    IDLE,
    IMMEDIATE;

    private GracefulShutdownResult() {
    }
}
