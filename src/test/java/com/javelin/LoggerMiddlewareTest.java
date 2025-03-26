package com.javelin;

import com.javelin.core.Middleware;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerMiddlewareTest {
    static VirtualThreadServer server;

    @BeforeAll
    static void setup() {
        server = new VirtualThreadServer(8080);

        AtomicBoolean logCalled = new AtomicBoolean(false);

        server.use(ctx -> {
            logCalled.set(true);
            ctx.next();
        });

        server.get("/", ctx -> ctx.send("Hello!"));
        server.start();

        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void tearDown() {
        try {
            server.stop();
        } catch (Exception ignored) {}
    }

    @Test
    void testLoggerMiddlewareCalled() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/").openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.readLine();
            assertEquals("Hello!", response);
        }
    }
}
