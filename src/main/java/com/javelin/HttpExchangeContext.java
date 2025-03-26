package com.javelin;

import com.javelin.core.Context;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpExchangeContext implements Context {
    private final HttpExchange exchange;
    private final Map<String, String> queryParams;

    public HttpExchangeContext(HttpExchange exchange) {
        this.exchange = exchange;
        this.queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
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

    @Override
    public String queryParam(String key) {
        return queryParams.get(key);
    }

    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> result = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return result;

        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String name = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            result.put(name, value);
        }

        return result;
    }

    private String decode(String value) {
        try {
            return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}