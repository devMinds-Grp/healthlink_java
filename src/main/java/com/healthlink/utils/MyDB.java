package com.healthlink.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {
    private final String url = "jdbc:mysql://localhost:3306/healthlink15";
    private final String user = "root";
    private final String password = "";
    private Connection conn;
    private static MyDB instance;

    // Constructeur privé pour le pattern Singleton
    private MyDB() {
        establishConnection();
    }

    // Méthode pour établir la connexion initiale
    private void establishConnection() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Successfully connected to database");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    // Méthode Singleton pour obtenir l'instance
    public static synchronized MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    // Méthode pour obtenir la connexion (à utiliser dans vos services)
    public Connection getConnection() throws SQLException {
        // Vérifie si la connexion est valide
        if (conn == null || conn.isClosed()) {
            reconnect();
        }
        return conn;
    }

    // Méthode pour reconnecter si la connexion est perdue
    private void reconnect() throws SQLException {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Successfully reconnected to database");
        } catch (SQLException e) {
            System.err.println("Failed to reconnect to database: " + e.getMessage());
            throw e;
        }
    }

    // Méthode pour fermer proprement la connexion
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