package com.javelin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatusCodeTest {

    static VirtualThreadServer server;

    @BeforeAll
    static void setup() {
        server = new VirtualThreadServer(8080);

        server.get("/unauthorized", ctx -> {
            ctx.status(401);
            ctx.send("Unauthorized");
        });

        server.get("/custom-json", ctx -> {
            ctx.status(418); // I'm a teapot
            ctx.json(java.util.Map.of("message", "I'm a teapot"));
        });

        server.start();
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
    }

    @AfterAll
    static void teardown() {
        try {
            server.stop();
        } catch (Exception ignored) {
        }
    }

    @Test
    void testPlainTextWithCustomStatus() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/unauthorized").openConnection();
        conn.setRequestMethod("GET");

        assertEquals(401, conn.getResponseCode());

        String response = readResponseBody(conn);
        assertEquals("Unauthorized", response);
    }

    @Test
    void testJsonWithCustomStatus() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/custom-json").openConnection();
        conn.setRequestMethod("GET");

        assertEquals(418, conn.getResponseCode());
        assertEquals("application/json", conn.getContentType());

        String response = readResponseBody(conn);
        assertTrue(response.contains("\"message\""));
    }

    private String readResponseBody(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream()
        ))) {
            return reader.readLine();
        }
    }
}
