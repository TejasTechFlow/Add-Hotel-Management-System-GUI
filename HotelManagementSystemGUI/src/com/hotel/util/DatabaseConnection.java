package com.hotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — Singleton utility that manages a single JDBC connection
 * to the MySQL hotel_management database.
 *
 * Usage:
 *   Connection conn = DatabaseConnection.getInstance().getConnection();
 */
public class DatabaseConnection {

    // ── Configuration ────────────────────────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/hotel_management"
                                         + "?useSSL=false&serverTimezone=UTC"
                                         + "&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";       // change as needed
    private static final String PASSWORD = ""; // change as needed

    // ── Singleton ────────────────────────────────────────────────────────────
    private static DatabaseConnection instance;
    private Connection connection;

    /** Private constructor — loads JDBC driver and opens connection. */
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connected to hotel_management database.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found. Add mysql-connector-j.jar to classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the single DatabaseConnection instance, creating it on first call.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Returns the underlying JDBC {@link Connection}. */
    public Connection getConnection() {
        return connection;
    }

    /** Silently checks whether the connection has been closed or is invalid. */
    private static boolean isConnectionClosed() {
        try {
            return instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    /** Closes the connection and resets the singleton — call on application exit. */
    public static void closeConnection() {
        if (instance != null) {
            try {
                if (!isConnectionClosed()) {
                    instance.connection.close();
                    System.out.println("[DB] Connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }
}
