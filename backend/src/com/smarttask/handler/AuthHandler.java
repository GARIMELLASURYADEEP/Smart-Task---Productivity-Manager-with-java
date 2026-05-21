package com.smarttask.handler;

import com.smarttask.dao.UserDAO;
import com.smarttask.model.User;
import com.smarttask.util.HttpUtil;
import com.smarttask.util.JsonUtil;
import com.smarttask.util.PasswordUtil;
import com.smarttask.util.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Handles signup, login, logout and current user API.
 */
public class AuthHandler implements HttpHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (path) {
                case "/api/auth/signup" -> handleSignup(exchange, method);
                case "/api/auth/login" -> handleLogin(exchange, method);
                case "/api/auth/logout" -> handleLogout(exchange, method);
                case "/api/auth/me" -> handleMe(exchange, method);
                default -> HttpUtil.sendJson(exchange, 404, JsonUtil.error("Auth route not found"));
            }
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, JsonUtil.error("Server error: " + e.getMessage()));
        }
    }

    private void handleSignup(HttpExchange exchange, String method) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtil.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
            return;
        }

        Map<String, String> body = JsonUtil.parseObject(HttpUtil.readRequestBody(exchange));
        String username = body.getOrDefault("username", "").trim();
        String email = body.getOrDefault("email", "").trim();
        String password = body.getOrDefault("password", "").trim();

        // Basic validation
        if (username.length() < 3) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Username must be at least 3 characters"));
            return;
        }
        if (!email.contains("@")) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Please enter a valid email"));
            return;
        }
        if (password.length() < 6) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Password must be at least 6 characters"));
            return;
        }

        if (userDAO.usernameExists(username)) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Username already exists"));
            return;
        }
        if (userDAO.emailExists(email)) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Email already registered"));
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        boolean saved = userDAO.registerUser(username, email, hashedPassword);

        if (!saved) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Signup failed. Try again."));
            return;
        }

        User user = userDAO.findByUsername(username);
        String sessionId = SessionManager.createSession(user.getId());
        HttpUtil.setSessionCookie(exchange, sessionId);

        String response = """
            {"success":true,"message":"Signup successful","user":{"id":%d,"username":"%s","email":"%s"}}
        """.formatted(user.getId(), JsonUtil.escape(user.getUsername()), JsonUtil.escape(user.getEmail()));

        HttpUtil.sendJson(exchange, 200, response);
    }

    private void handleLogin(HttpExchange exchange, String method) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtil.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
            return;
        }

        Map<String, String> body = JsonUtil.parseObject(HttpUtil.readRequestBody(exchange));
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();

        if (username.isBlank() || password.isBlank()) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Username and password are required"));
            return;
        }

        User user = userDAO.findByUsername(username);
        if (user == null || !PasswordUtil.verifyPassword(password, user.getPassword())) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.error("Invalid username or password"));
            return;
        }

        String sessionId = SessionManager.createSession(user.getId());
        HttpUtil.setSessionCookie(exchange, sessionId);

        String response = """
            {"success":true,"message":"Login successful","user":{"id":%d,"username":"%s","email":"%s"}}
        """.formatted(user.getId(), JsonUtil.escape(user.getUsername()), JsonUtil.escape(user.getEmail()));

        HttpUtil.sendJson(exchange, 200, response);
    }

    private void handleLogout(HttpExchange exchange, String method) throws IOException {
        if (!"POST".equalsIgnoreCase(method)) {
            HttpUtil.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
            return;
        }

        String sessionId = HttpUtil.getSessionId(exchange);
        SessionManager.removeSession(sessionId);
        HttpUtil.clearSessionCookie(exchange);

        HttpUtil.sendJson(exchange, 200, JsonUtil.success("Logged out successfully"));
    }

    private void handleMe(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            HttpUtil.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
            return;
        }

        int userId = SessionManager.getUserId(HttpUtil.getSessionId(exchange));
        if (userId <= 0) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.error("Not logged in"));
            return;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.error("User not found"));
            return;
        }

        String response = """
            {"success":true,"user":{"id":%d,"username":"%s","email":"%s"}}
        """.formatted(user.getId(), JsonUtil.escape(user.getUsername()), JsonUtil.escape(user.getEmail()));

        HttpUtil.sendJson(exchange, 200, response);
    }
}
