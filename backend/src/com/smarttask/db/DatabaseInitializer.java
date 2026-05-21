package com.smarttask.db;

import com.smarttask.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates database tables and inserts sample data for demo/testing.
 */
public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL
                )
            """);

            // Create tasks table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    priority TEXT DEFAULT 'Medium',
                    due_date TEXT,
                    status TEXT DEFAULT 'pending',
                    created_at TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Insert demo user only if users table is empty
            var rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM users");
            int userCount = rs.getInt("cnt");
            rs.close();

            if (userCount == 0) {
                insertSampleData(stmt);
                System.out.println("Sample data inserted successfully.");
            } else {
                System.out.println("Database already has data. Skipping sample insert.");
            }

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Demo account: username = demo, password = demo123
     */
    private static void insertSampleData(Statement stmt) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword("demo123");

        stmt.executeUpdate("""
            INSERT INTO users (username, email, password)
            VALUES ('demo', 'demo@smarttask.com', '%s')
        """.formatted(hashedPassword));

        // Demo user id will be 1 for fresh database
        stmt.executeUpdate("""
            INSERT INTO tasks (user_id, title, description, priority, due_date, status, created_at)
            VALUES
            (1, 'Complete Java Assignment', 'Finish JDBC project documentation', 'High', date('now', '+2 days'), 'pending', datetime('now', '-2 days')),
            (1, 'Prepare Resume', 'Update resume with latest project', 'Medium', date('now', '+5 days'), 'pending', datetime('now', '-1 days')),
            (1, 'Team Meeting Notes', 'Share meeting summary with team', 'Low', date('now', '-1 days'), 'completed', datetime('now', '-3 days')),
            (1, 'Practice SQL Queries', 'Revise SELECT, INSERT, UPDATE, DELETE', 'High', date('now', '-2 days'), 'pending', datetime('now', '-4 days')),
            (1, 'Read Java Documentation', 'Study HttpServer and JDBC basics', 'Medium', date('now'), 'completed', datetime('now', '-5 days'))
        """);
    }
}
