package com.healthlink.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDB {
    private final String url = "jdbc:mysql://localhost:3306/healthlink15";
    private final String user = "root";
    private final String password = "";
    private Connection conn;

    // Private constructor to prevent direct instantiation
    private MyDB() throws SQLException {
        establishConnection();
        createCategorieTableIfNotExists();
        createDonDuSangTableIfNotExists(); // Add table creation for don_du_sang
    }

    // Establish connection to the database
    private void establishConnection() throws SQLException {
        conn = DriverManager.getConnection(url, user, password);
        System.out.println("Successfully connected to database");
    }

    // Create the categorie table if it doesn't exist
    private void createCategorieTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS categorie (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nom VARCHAR(100) NOT NULL
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'categorie' verified/created successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Error creating table 'categorie': " + e.getMessage());
            throw new RuntimeException("Failed to create categorie table", e);
        }
    }

    // Create the don_du_sang table if it doesn't exist
    private void createDonDuSangTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS don_du_sang (
                id INT AUTO_INCREMENT PRIMARY KEY,
                description VARCHAR(255),
                lieu VARCHAR(100),
                date DATE,
                num_tel VARCHAR(20)
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'don_du_sang' verified/created successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Error creating table 'don_du_sang': " + e.getMessage());
            throw new RuntimeException("Failed to create don_du_sang table", e);
        }
    }

    // Singleton pattern using initialization-on-demand holder idiom
    private static class SingletonHolder {
        private static MyDB instance;

        static {
            try {
                instance = new MyDB();
            } catch (SQLException e) {
                System.err.println("❌ Failed to initialize MyDB instance: " + e.getMessage());
                throw new RuntimeException("Database initialization failed", e);
            }
        }
    }

    public static MyDB getInstance() {
        return SingletonHolder.instance;
    }

    // Provide the active connection, reconnect if necessary
    public Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            reconnect();
        }
        return conn;
    }

    // Reconnect to the database
    private void reconnect() throws SQLException {
        System.out.println("Attempting to reconnect to database...");
        conn = DriverManager.getConnection(url, user, password);
        System.out.println("Successfully reconnected to database");
    }

    // Close the connection properly
    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed successfully");
            }
        } catch (SQLException e) {
            System.err.println("Error while closing connection: " + e.getMessage());
        }
    }
}