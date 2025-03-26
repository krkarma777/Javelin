package com.javelin;

import com.javelin.core.ExceptionHandler;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionHandlerTest {

    static VirtualThreadServer server;

    @BeforeAll
    static void setup() {
        server = new VirtualThreadServer(8080);

        // 예외 핸들러 설정
        server.setExceptionHandler((e, ctx) -> {
            ctx.status(500);
            ctx.send("Custom Error: " + e.getClass().getSimpleName());
        });

        // 예외를 강제로 발생시키는 라우트
        server.get("/error", ctx -> {
            throw new NullPointerException("Boom!");
        });

        server.start();
        try {
            Thread.sleep(300); // 서버 뜰 시간
        } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void tearDown() {
        try {
            server.stop();
        } catch (Exception ignored) {}
    }

    @Test
    void testCustomExceptionHandler() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/error").openConnection();
        conn.setRequestMethod("GET");

        assertEquals(500, conn.getResponseCode());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
            String response = reader.readLine();
            assertEquals("Custom Error: NullPointerException", response);
        }
    }
}
