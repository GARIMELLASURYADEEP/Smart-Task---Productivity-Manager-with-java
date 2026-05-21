package com.smarttask.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory session manager.
 * Maps session id (cookie value) -> logged in user id.
 */
public class SessionManager {

    private static final Map<String, Integer> sessions = new ConcurrentHashMap<>();

    /**
     * Create new session after successful login/signup.
     */
    public static String createSession(int userId) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, userId);
        return sessionId;
    }

    /**
     * Get user id from session id. Returns -1 if session invalid.
     */
    public static int getUserId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return -1;
        }
        return sessions.getOrDefault(sessionId, -1);
    }

    /**
     * Remove session on logout.
     */
    public static void removeSession(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }
}
