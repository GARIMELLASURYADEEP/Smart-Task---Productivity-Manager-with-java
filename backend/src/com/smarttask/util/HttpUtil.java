package com.smarttask.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Common HTTP helper methods used by handlers.
 */
public class HttpUtil {

    public static void sendJson(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Read query parameters from request URI.
     */
    public static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return params;
        }

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], decode(kv[1]));
            }
        }
        return params;
    }

    /**
     * Get session id from Cookie header.
     */
    public static String getSessionId(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) {
            return null;
        }

        for (String part : cookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("SESSION_ID=")) {
                return trimmed.substring("SESSION_ID=".length());
            }
        }
        return null;
    }

    /**
     * Attach session cookie to response.
     */
    public static void setSessionCookie(HttpExchange exchange, String sessionId) {
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                "SESSION_ID=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax"
        );
    }

    /**
     * Clear session cookie on logout.
     */
    public static void clearSessionCookie(HttpExchange exchange) {
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                "SESSION_ID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
        );
    }

    private static String decode(String value) {
        return value.replace("+", " ").replace("%20", " ");
    }
}
