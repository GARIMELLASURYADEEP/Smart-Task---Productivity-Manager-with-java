package com.smarttask.dao;

import com.smarttask.db.DatabaseConnection;
import com.smarttask.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task CRUD operations using JDBC PreparedStatement.
 */
public class TaskDAO {

    /**
     * Create a new task for logged-in user.
     */
    public boolean addTask(Task task) {
        String sql = """
            INSERT INTO tasks (user_id, title, description, priority, due_date, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, task.getUserId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getPriority());
            ps.setString(5, task.getDueDate());
            ps.setString(6, task.getStatus());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update existing task (only if it belongs to user).
     */
    public boolean updateTask(Task task) {
        String sql = """
            UPDATE tasks
            SET title = ?, description = ?, priority = ?, due_date = ?, status = ?
            WHERE id = ? AND user_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority());
            ps.setString(4, task.getDueDate());
            ps.setString(5, task.getStatus());
            ps.setInt(6, task.getId());
            ps.setInt(7, task.getUserId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete task by id for specific user.
     */
    public boolean deleteTask(int taskId, int userId) {
        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark task as completed.
     */
    public boolean markCompleted(int taskId, int userId) {
        String sql = "UPDATE tasks SET status = 'completed' WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get single task by id.
     */
    public Task getTaskById(int taskId, int userId) {
        String sql = "SELECT * FROM tasks WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTask(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get tasks with optional search and filters.
     */
    public List<Task> getTasks(int userId, String search, String priority, String status, String dueDateFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM tasks WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (search != null && !search.isBlank()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            String keyword = "%" + search + "%";
            params.add(keyword);
            params.add(keyword);
        }

        if (priority != null && !priority.isBlank() && !priority.equalsIgnoreCase("all")) {
            sql.append(" AND priority = ?");
            params.add(priority);
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        // Simple due date filter options
        if (dueDateFilter != null && !dueDateFilter.isBlank() && !dueDateFilter.equalsIgnoreCase("all")) {
            switch (dueDateFilter.toLowerCase()) {
                case "today" -> sql.append(" AND due_date = date('now')");
                case "overdue" -> sql.append(" AND due_date < date('now') AND status != 'completed'");
                case "upcoming" -> sql.append(" AND due_date > date('now')");
                default -> {
                }
            }
        }

        sql.append(" ORDER BY created_at DESC");

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapTask(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    /**
     * Dashboard statistics for cards.
     */
    public Map<String, Integer> getDashboardStats(int userId) {
        Map<String, Integer> stats = new HashMap<>();

        String sql = """
            SELECT
                COUNT(*) AS total,
                SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) AS completed,
                SUM(CASE WHEN status != 'completed' THEN 1 ELSE 0 END) AS pending,
                SUM(CASE WHEN due_date < date('now') AND status != 'completed' THEN 1 ELSE 0 END) AS overdue
            FROM tasks
            WHERE user_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("total", rs.getInt("total"));
                    stats.put("completed", rs.getInt("completed"));
                    stats.put("pending", rs.getInt("pending"));
                    stats.put("overdue", rs.getInt("overdue"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Priority distribution for pie chart.
     */
    public Map<String, Integer> getPriorityStats(int userId) {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT priority, COUNT(*) AS count FROM tasks WHERE user_id = ? GROUP BY priority";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("priority"), rs.getInt("count"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Status distribution for chart.
     */
    public Map<String, Integer> getStatusStats(int userId) {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS count FROM tasks WHERE user_id = ? GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("status"), rs.getInt("count"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Weekly completed tasks count (last 7 days) for line chart.
     */
    public List<Map<String, Object>> getWeeklyProductivity(int userId) {
        List<Map<String, Object>> weeklyData = new ArrayList<>();

        String sql = """
            SELECT date(created_at) AS day,
                   SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) AS completed_count,
                   COUNT(*) AS total_count
            FROM tasks
            WHERE user_id = ?
              AND date(created_at) >= date('now', '-6 days')
            GROUP BY date(created_at)
            ORDER BY day ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("day", rs.getString("day"));
                    row.put("completed", rs.getInt("completed_count"));
                    row.put("total", rs.getInt("total_count"));
                    weeklyData.add(row);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return weeklyData;
    }

    private Task mapTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setUserId(rs.getInt("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setPriority(rs.getString("priority"));
        task.setDueDate(rs.getString("due_date"));
        task.setStatus(rs.getString("status"));
        task.setCreatedAt(rs.getString("created_at"));
        return task;
    }
}
