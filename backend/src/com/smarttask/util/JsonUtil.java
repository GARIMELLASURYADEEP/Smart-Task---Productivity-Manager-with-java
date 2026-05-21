package com.smarttask.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very small JSON helper for this project.
 * Avoids extra libraries and keeps code beginner-friendly.
 */
public class JsonUtil {

    private static final Pattern STRING_FIELD = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern NUMBER_FIELD = Pattern.compile("\"(\\w+)\"\\s*:\\s*(-?\\d+)");

    /**
     * Parse simple JSON object into key-value map.
     * Supports string and integer values only (enough for this app).
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank()) {
            return map;
        }

        Matcher stringMatcher = STRING_FIELD.matcher(json);
        while (stringMatcher.find()) {
            map.put(stringMatcher.group(1), unescape(stringMatcher.group(2)));
        }

        Matcher numberMatcher = NUMBER_FIELD.matcher(json);
        while (numberMatcher.find()) {
            map.put(numberMatcher.group(1), numberMatcher.group(2));
        }

        return map;
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static String success(String message) {
        return "{\"success\":true,\"message\":\"" + escape(message) + "\"}";
    }

    public static String error(String message) {
        return "{\"success\":false,\"message\":\"" + escape(message) + "\"}";
    }

    private static String unescape(String value) {
        return value
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
