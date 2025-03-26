package com.javelin;

import com.javelin.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextBodyTest {
    static VirtualThreadServer server;

    @BeforeAll
    static void setup() {
        server = new VirtualThreadServer(8080);
        server.post("/user", ctx -> {
            User user = ctx.body(User.class);
            ctx.send("User: " + user.id() + ", " + user.name());
        });
        server.start();

        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void teardown() {
        try { server.stop(); } catch (Exception ignored) {}
    }

    @Test
    void testPostJsonBody() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/user").openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String json = "{\"id\":42,\"name\":\"Javelin\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        assertEquals(200, conn.getResponseCode());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.readLine();
            assertEquals("User: 42, Javelin", response);
        }
    }
}
