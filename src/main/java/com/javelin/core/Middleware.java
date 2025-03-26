package com.javelin.core;

@FunctionalInterface
public interface Middleware {
    void handle(Context ctx) throws Exception;
}