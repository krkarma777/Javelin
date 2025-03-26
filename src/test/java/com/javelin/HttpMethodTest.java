package com.javelin;

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpMethodTest {
    static VirtualThreadServer server;

    @BeforeAll
    static void setup() {
        server = new VirtualThreadServer(8080);

        server.put("/update", ctx -> ctx.send("PUT OK"));
        server.delete("/remove", ctx -> ctx.send("DELETE OK"));
        server.patch("/patch", ctx -> ctx.send("PATCH OK"));
        server.head("/ping", ctx -> ctx.send("")); // HEAD는 본문 없이 응답

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
    void testPutRoute() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/update").openConnection();
        conn.setRequestMethod("PUT");

        assertEquals(200, conn.getResponseCode());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            assertEquals("PUT OK", reader.readLine());
        }
    }

    @Test
    void testDeleteRoute() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/remove").openConnection();
        conn.setRequestMethod("DELETE");

        assertEquals(200, conn.getResponseCode());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            assertEquals("DELETE OK", reader.readLine());
        }
    }

    @Test
    void testPatchRoute() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/patch").openConnection();
        conn.setRequestMethod("POST"); // 실제로는 POST로 전송
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH"); // 서버에서 PATCH로 인식

        assertEquals(200, conn.getResponseCode());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            assertEquals("PATCH OK", reader.readLine());
        }
    }

    @Test
    void testHeadRoute() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/ping").openConnection();
        conn.setRequestMethod("HEAD");

        assertEquals(200, conn.getResponseCode());

        // Java HttpURLConnection은 Content-Length가 -1일 수 있으므로 0 이상으로 검증
        assertTrue(conn.getContentLengthLong() >= 0);
    }
}
