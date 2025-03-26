package com.javelin;

import com.javelin.core.StaticFileHandler;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaticFileHandlerTest {
    static VirtualThreadServer server;

    @BeforeAll
    static void setup() throws Exception {
        server = new VirtualThreadServer(8080);
        server.use(new StaticFileHandler("/static", "src/test/resources/public"));
        server.start();
        Thread.sleep(300); // give the server time to start
    }

    @AfterAll
    static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    void testStaticTextFile() throws Exception {
        URL url = new URL("http://localhost:8080/static/hello.txt");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        assertEquals("text/plain", conn.getHeaderField("Content-Type"));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            assertEquals("Hello Static!", reader.readLine());
        }
    }

    @Test
    void testNonExistentFile() throws Exception {
        URL url = new URL("http://localhost:8080/static/missing.txt");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(404, conn.getResponseCode());
    }
}
