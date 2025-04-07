package com.javelin;

public class JavelinApplication {
    public static void run(Class<? extends JavelinAppInitializer> appClass, int port) {
        try {
            JavelinAppInitializer app = appClass.getDeclaredConstructor().newInstance();
            VirtualThreadServer server = new VirtualThreadServer(port);
            app.initialize(server);
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch application", e);
        }
    }
}