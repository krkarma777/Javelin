package com.javelin;

import com.javelin.core.Context;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ContextTest {

    static VirtualThreadServer server;

    @BeforeAll
    static void setUp() {
        server = new VirtualThreadServer(8080);
        server.get("/user", ctx -> {
            Map<String, Object> user = Map.of("id", 1, "name", "Javelin");
            ctx.json(user);
        });
        server.start();

        waitForServerStartup();
    }

    @AfterAll
    static void tearDown() {
        try {
            server.stop();
        } catch (Exception ignored) {}
    }

    @Test
    void testQueryParamParsing() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestURI()).thenReturn(URI.create("/test?id=hello&lang=kr"));

        Context ctx = new HttpExchangeContext(exchange);

        assertEquals("hello", ctx.queryParam("id"));
        assertEquals("kr", ctx.queryParam("lang"));
    }

    @Test
    void testJsonRoute() throws Exception {
        HttpURLConnection conn = openGetConnection("http://localhost:8080/user");

        assertEquals(200, conn.getResponseCode());
        assertEquals("application/json", conn.getContentType());

        String response = readResponse(conn);
        assertTrue(response.contains("\"id\":1"));
        assertTrue(response.contains("\"name\":\"Javelin\""));
    }

    // ===================== 유틸 =====================

    private static HttpURLConnection openGetConnection(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        return conn;
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return reader.readLine();
        }
    }

    private static void waitForServerStartup() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
    }
}
