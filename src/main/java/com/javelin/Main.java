package com.javelin;

public class Main {
    public static void main(String[] args) {
        VirtualThreadServer server = new VirtualThreadServer(8080);
        server.start();
    }
}
