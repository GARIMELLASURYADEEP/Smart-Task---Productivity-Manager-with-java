package com.smarttask.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves frontend HTML, CSS, JS and image files.
 */
public class StaticFileHandler implements HttpHandler {

    private static final String FRONTEND_FOLDER = "frontend";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();

        // Default page
        if ("/".equals(requestPath) || requestPath.isBlank()) {
            requestPath = "/index.html";
        }

        // Security: prevent directory traversal
        if (requestPath.contains("..")) {
            sendNotFound(exchange);
            return;
        }

        Path filePath = Paths.get(FRONTEND_FOLDER + requestPath);

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendNotFound(exchange);
            return;
        }

        String contentType = getContentType(filePath.toString());
        byte[] fileBytes = Files.readAllBytes(filePath);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, fileBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileBytes);
        }
    }

    private void sendNotFound(HttpExchange exchange) throws IOException {
        String message = "404 - File Not Found";
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }
}
