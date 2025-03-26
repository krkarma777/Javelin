package com.javelin;

import com.javelin.core.Context;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class HttpExchangeContext implements Context {
    private final HttpExchange exchange;

    public HttpExchangeContext(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String path() {
        return exchange.getRequestURI().getPath();
    }

    @Override
    public void send(String body) {
        try {
            byte[] bytes = body.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }
}