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
    private static MyDB instance;

    // Constructeur privé pour le pattern Singleton
    private MyDB() {
        establishConnection();
        createCategorieTableIfNotExists(); // Ajout ici
    }

    // Connexion initiale à la base
    private void establishConnection() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Successfully connected to database");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    // Création automatique de la table categorie si elle n'existe pas
    private void createCategorieTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS categorie (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nom VARCHAR(100) NOT NULL
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'categorie' vérifiée/créée avec succès.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de la table 'categorie': " + e.getMessage());
        }
    }

    // Instance Singleton
    public static synchronized MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    // Fournit la connexion active
    public Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            reconnect();
        }
        return conn;
    }

    // Reconnexion
    private void reconnect() throws SQLException {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Successfully reconnected to database");
        } catch (SQLException e) {
            System.err.println("Failed to reconnect to database: " + e.getMessage());
            throw e;
        }
    }

    // Fermer proprement la connexion
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
<<<<<<< HEAD
}
=======
}
>>>>>>> master
