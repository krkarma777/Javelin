package com.javelin.core;

public interface Context {
    String path();
    void send(String body);
}
