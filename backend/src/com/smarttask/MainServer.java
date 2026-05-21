package com.smarttask;

import com.smarttask.db.DatabaseInitializer;
import com.smarttask.handler.AuthHandler;
import com.smarttask.handler.StaticFileHandler;
import com.smarttask.handler.TaskHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

/**
 * Main entry point - starts Java HTTP server.
 *
 * Run from project root:
 * java -cp "backend/lib/sqlite-jdbc.jar;backend/out" com.smarttask.MainServer
 */
public class MainServer {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            // Step 1: Initialize SQLite database and tables
            DatabaseInitializer.initialize();

            // Step 2: Create HTTP server
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // API routes
            server.createContext("/api/auth", new AuthHandler());
            server.createContext("/api/tasks", new TaskHandler());
            server.createContext("/api/dashboard", new TaskHandler());
            server.createContext("/api/analytics", new TaskHandler());

            // Static frontend files (HTML/CSS/JS)
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("==============================================");
            System.out.println(" Smart Task Manager Server Started");
            System.out.println(" URL: http://localhost:" + PORT);
            System.out.println(" Demo Login -> username: demo | password: demo123");
            System.out.println("==============================================");

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
