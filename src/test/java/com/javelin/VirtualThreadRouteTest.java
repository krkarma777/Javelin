package com.javelin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VirtualThreadRouteTest {
    static VirtualThreadServer server;

    @BeforeAll
    static void setup() {
        server = new VirtualThreadServer(8080);
        server.get("/", ctx -> ctx.send("Hello Javelin!"));
        server.get("/hi", ctx -> ctx.send("Hi there!"));
        server.start();

        // wait for server to be ready (간단 대기)
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void teardown() {
        try {
            server.stop();
        } catch (Exception ignored) {}
    }

    @Test
    void testRootRoute() throws Exception {
        String response = sendHttpRequest("http://localhost:8080/");
        assertEquals("Hello Javelin!", response);
    }

    @Test
    void testHiRoute() throws Exception {
        String response = sendHttpRequest("http://localhost:8080/hi");
        assertEquals("Hi there!", response);
    }

    @Test
    void testNotFoundRoute() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/unknown").openConnection();
        conn.setRequestMethod("GET");

        int statusCode = conn.getResponseCode();
        assertEquals(404, statusCode);
    }

    private String sendHttpRequest(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return reader.readLine();
        }
    }
}