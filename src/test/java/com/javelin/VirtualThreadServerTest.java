package com.javelin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualThreadServerTest {
    private static final int PORT = 8080;
    private static VirtualThreadServer server;
    private static boolean serverStarted = false;

    @BeforeAll
    static void setUp() {
        server = new VirtualThreadServer(PORT);
        new Thread(server::start).start();

        // 서버가 완전히 시작될 때까지 대기하고 결과를 serverStarted에 할당
        serverStarted = waitForServerToStart();
    }

    @AfterAll
    static void tearDown() {
        if (server != null) {
            server.stop();
            waitForServerToStop();
        }

        // 서버 종료 후 추가 대기 시간 (2초)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    @Test
    void testSingleRequest() throws Exception {
        if (!serverStarted) {
            throw new RuntimeException("Server did not start properly. Skipping test.");
        }

        String response = sendHttpRequest("http://localhost:" + PORT);
        assertEquals("Hello, Virtual Thread!", response);
    }

    @Test
    void testConcurrentRequests() throws Exception {
        if (!serverStarted) {
            throw new RuntimeException("Server did not start properly. Skipping test.");
        }

        int threadCount = 100; // 동시 요청 수
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        Future<?>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> executor.submit(() -> {
                    try {
                        String response = sendHttpRequest("http://localhost:" + PORT);
                        assertEquals("Hello, Virtual Thread!", response);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }))
                .toArray(Future[]::new);

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
    }

    @Test
    void testServerHandlesLoadGracefully() throws Exception {
        if (!serverStarted) {
            throw new RuntimeException("Server did not start properly. Skipping test.");
        }

        int threadCount = 150; // 요청 개수 조절
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        long startTime = System.currentTimeMillis();

        List<Future<String>> futures = IntStream.range(0, threadCount)
                .mapToObj(i -> executor.submit(() -> {
                    try {
                        return sendHttpRequest("http://localhost:" + PORT);
                    } catch (Exception e) {
                        return "ERROR"; // 실패한 요청을 로그로 남김
                    }
                }))
                .toList();

        int successCount = 0;
        for (Future<String> future : futures) {
            if ("Hello, Virtual Thread!".equals(future.get())) {
                successCount++;
            }
        }

        executor.shutdown();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Processed " + successCount + "/" + threadCount + " requests successfully in " + duration + " ms");

        assertTrue(successCount > threadCount * 0.9, "응답 성공률이 너무 낮습니다!");
    }


    /**
     * 서버가 정상적으로 실행될 때까지 대기
     */
    private static boolean waitForServerToStart() {
        int maxRetries = 30;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Thread.sleep(1000);
                String response = sendHttpRequest("http://localhost:" + PORT);
                if (response.contains("Hello, Virtual Thread!")) {
                    System.out.println("✅ Server is up and running!");
                    return true; // 서버가 정상적으로 시작됨
                }
            } catch (Exception e) {
                System.out.println("⏳ Waiting for server to start... (Retry " + (retryCount + 1) + ")");
            }
            retryCount++;
        }

        throw new RuntimeException("❌ Server failed to start within the timeout period!");
    }

    /**
     * 서버가 정상적으로 종료될 때까지 대기
     */
    private static void waitForServerToStop() {
        int maxRetries = 10;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Thread.sleep(500); // 1초 → 0.5초로 줄이기
                sendHttpRequest("http://localhost:" + PORT);
            } catch (Exception e) {
                System.out.println("✅ Server has stopped.");
                return;
            }
            retryCount++;
        }

        System.err.println("❌ Server failed to stop properly!");
    }

    /**
     * HTTP GET 요청을 보내고 응답을 받음 (정적 메서드로 변경)
     */
    private static String sendHttpRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return in.readLine();
        }
    }
}
