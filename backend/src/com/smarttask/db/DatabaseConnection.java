package com.smarttask.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC connection helper for SQLite database.
 * Keeps database path simple and easy to understand.
 */
public class DatabaseConnection {

    // SQLite database file stored inside backend/database folder
    private static final String DB_FOLDER = "backend/database";
    private static final String DB_FILE = "tasks.db";
    private static String dbUrl;

    static {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Build absolute path so app works from project root
            File dbDir = new File(DB_FOLDER);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            File dbFile = new File(DB_FOLDER, DB_FILE);
            dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            System.out.println("Database path: " + dbFile.getAbsolutePath());

        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Add sqlite-jdbc.jar to classpath.");
            e.printStackTrace();
        }
    }

    /**
     * Returns a new database connection.
     * Always close connection after use (try-with-resources recommended).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}
