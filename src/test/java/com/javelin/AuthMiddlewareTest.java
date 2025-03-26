package com.javelin;

import com.javelin.core.Middleware;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class AuthMiddlewareTest {
    static VirtualThreadServer server;

    @BeforeAll
    static void setup() {
        server = new VirtualThreadServer(8080);

        // 인증 미들웨어 - "Authorization" 헤더가 없으면 401 반환
        server.use(ctx -> {
            String auth = ctx.header("Authorization");
            if (auth == null || auth.isBlank()) {
                ctx.send("Unauthorized");
                return;
            }
            ctx.next();
        });

        server.get("/secure", ctx -> ctx.send("Welcome!"));
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
    void testUnauthorizedAccessBlocked() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/secure").openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.readLine();
            assertEquals("Unauthorized", response);
        }
    }
}
