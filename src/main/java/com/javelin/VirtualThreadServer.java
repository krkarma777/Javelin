package com.javelin;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class VirtualThreadServer {
    private final int port;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public VirtualThreadServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🚀 Javelin WAS started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleRequest(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // HTTP Request 파싱
            String requestLine = in.readLine();
            if (requestLine == null) return;
            System.out.println("📥 Received request: " + requestLine);

            // 간단한 HTTP Response 전송
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("Hello from Javelin Virtual Thread WAS!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
