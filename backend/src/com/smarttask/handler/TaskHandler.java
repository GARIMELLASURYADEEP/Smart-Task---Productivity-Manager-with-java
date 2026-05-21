package com.smarttask.handler;

import com.smarttask.dao.TaskDAO;
import com.smarttask.model.Task;
import com.smarttask.util.HttpUtil;
import com.smarttask.util.JsonUtil;
import com.smarttask.util.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles task CRUD, dashboard stats and analytics APIs.
 */
public class TaskHandler implements HttpHandler {

    private final TaskDAO taskDAO = new TaskDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        int userId = SessionManager.getUserId(HttpUtil.getSessionId(exchange));
        if (userId <= 0) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.error("Please login first"));
            return;
        }

        try {
            if (path.equals("/api/dashboard")) {
                handleDashboard(exchange, method, userId);
            } else if (path.equals("/api/analytics")) {
                handleAnalytics(exchange, method, userId);
            } else if (path.equals("/api/tasks")) {
                handleTasks(exchange, method, userId);
            } else if (path.startsWith("/api/tasks/")) {
                handleTaskById(exchange, method, userId, path);
            } else {
                HttpUtil.sendJson(exchange, 404, JsonUtil.error("Task route not found"));
            }
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, JsonUtil.error("Server error: " + e.getMessage()));
        }
    }

    private void handleDashboard(HttpExchange exchange, String method, int userId) throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            HttpUtil.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
            return;
        }

        Map<String, Integer> stats = taskDAO.getDashboardStats(userId);
        int total = stats.getOrDefault("total", 0);
        int completed = stats.getOrDefault("completed", 0);
        int pending = stats.getOrDefault("pending", 0);
        int overdue = stats.getOrDefault("overdue", 0);

        int productivity = 0;
        if (total > 0) {
            productivity = (completed * 100) / total;
        }

        List<Task> recentTasks = taskDAO.getTasks(userId, null, null, null, null);
        if (recentTasks.size() > 5) {
            recentTasks = recentTasks.subList(0, 5);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"stats\":{");
        json.append("\"total\":").append(total).append(",");
        json.append("\"completed\":").append(completed).append(",");
        json.append("\"pending\":").append(pending).append(",");
        json.append("\"overdue\":").append(overdue).append(",");
        json.append("\"productivity\":").append(productivity);
        json.append("},\"recentTasks\":").append(tasksToJson(recentTasks)).append("}");

        HttpUtil.sendJson(exchange, 200, json.toString());
    }

    private void handleAnalytics(HttpExchange exchange, String method, int userId) throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            HttpUtil.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
            return;
        }

        Map<String, Integer> priorityStats = taskDAO.getPriorityStats(userId);
        Map<String, Integer> statusStats = taskDAO.getStatusStats(userId);
        List<Map<String, Object>> weekly = taskDAO.getWeeklyProductivity(userId);

        String json = """
            {"success":true,
             "priority":%s,
             "status":%s,
             "weekly":%s}
        """.formatted(
                mapToJson(priorityStats),
                mapToJson(statusStats),
                weeklyToJson(weekly)
        );

        HttpUtil.sendJson(exchange, 200, json);
    }

    private void handleTasks(HttpExchange exchange, String method, int userId) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
            List<Task> tasks = taskDAO.getTasks(
                    userId,
                    query.get("search"),
                    query.get("priority"),
                    query.get("status"),
                    query.get("dueDate")
            );
            HttpUtil.sendJson(exchange, 200, "{\"success\":true,\"tasks\":" + tasksToJson(tasks) + "}");
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            Map<String, String> body = JsonUtil.parseObject(HttpUtil.readRequestBody(exchange));

            String title = body.getOrDefault("title", "").trim();
            if (title.isBlank()) {
                HttpUtil.sendJson(exchange, 400, JsonUtil.error("Task title is required"));
                return;
            }

            Task task = new Task();
            task.setUserId(userId);
            task.setTitle(title);
            task.setDescription(body.getOrDefault("description", ""));
            task.setPriority(body.getOrDefault("priority", "Medium"));
            task.setDueDate(body.getOrDefault("dueDate", ""));
            task.setStatus(body.getOrDefault("status", "pending"));

            if (taskDAO.addTask(task)) {
                HttpUtil.sendJson(exchange, 201, JsonUtil.success("Task added successfully"));
            } else {
                HttpUtil.sendJson(exchange, 500, JsonUtil.error("Failed to add task"));
            }
            return;
        }

        HttpUtil.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
    }

    private void handleTaskById(HttpExchange exchange, String method, int userId, String path) throws IOException {
        // Example paths:
        // /api/tasks/5
        // /api/tasks/5/complete
        String[] parts = path.split("/");
        if (parts.length < 4) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Invalid task URL"));
            return;
        }

        int taskId;
        try {
            taskId = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.error("Invalid task id"));
            return;
        }

        boolean completeRoute = parts.length >= 5 && "complete".equals(parts[4]);

        if (completeRoute && "PUT".equalsIgnoreCase(method)) {
            if (taskDAO.markCompleted(taskId, userId)) {
                HttpUtil.sendJson(exchange, 200, JsonUtil.success("Task marked as completed"));
            } else {
                HttpUtil.sendJson(exchange, 404, JsonUtil.error("Task not found"));
            }
            return;
        }

        if ("PUT".equalsIgnoreCase(method)) {
            Map<String, String> body = JsonUtil.parseObject(HttpUtil.readRequestBody(exchange));

            Task existing = taskDAO.getTaskById(taskId, userId);
            if (existing == null) {
                HttpUtil.sendJson(exchange, 404, JsonUtil.error("Task not found"));
                return;
            }

            existing.setTitle(body.getOrDefault("title", existing.getTitle()));
            existing.setDescription(body.getOrDefault("description", existing.getDescription()));
            existing.setPriority(body.getOrDefault("priority", existing.getPriority()));
            existing.setDueDate(body.getOrDefault("dueDate", existing.getDueDate()));
            existing.setStatus(body.getOrDefault("status", existing.getStatus()));

            if (taskDAO.updateTask(existing)) {
                HttpUtil.sendJson(exchange, 200, JsonUtil.success("Task updated successfully"));
            } else {
                HttpUtil.sendJson(exchange, 500, JsonUtil.error("Failed to update task"));
            }
            return;
        }

        if ("DELETE".equalsIgnoreCase(method)) {
            if (taskDAO.deleteTask(taskId, userId)) {
                HttpUtil.sendJson(exchange, 200, JsonUtil.success("Task deleted successfully"));
            } else {
                HttpUtil.sendJson(exchange, 404, JsonUtil.error("Task not found"));
            }
            return;
        }

        HttpUtil.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
    }

    private String tasksToJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (i > 0) sb.append(",");
            sb.append(taskToJson(t));
        }
        sb.append("]");
        return sb.toString();
    }

    private String taskToJson(Task t) {
        return """
            {"id":%d,"userId":%d,"title":"%s","description":"%s","priority":"%s","dueDate":"%s","status":"%s","createdAt":"%s"}
        """.formatted(
                t.getId(),
                t.getUserId(),
                JsonUtil.escape(t.getTitle()),
                JsonUtil.escape(t.getDescription() == null ? "" : t.getDescription()),
                JsonUtil.escape(t.getPriority()),
                JsonUtil.escape(t.getDueDate() == null ? "" : t.getDueDate()),
                JsonUtil.escape(t.getStatus()),
                JsonUtil.escape(t.getCreatedAt() == null ? "" : t.getCreatedAt())
        );
    }

    private String mapToJson(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(JsonUtil.escape(entry.getKey())).append("\":").append(entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    private String weeklyToJson(List<Map<String, Object>> weekly) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < weekly.size(); i++) {
            Map<String, Object> row = weekly.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"day\":\"")
                    .append(JsonUtil.escape(String.valueOf(row.get("day"))))
                    .append("\",\"completed\":")
                    .append(row.get("completed"))
                    .append(",\"total\":")
                    .append(row.get("total"))
                    .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
