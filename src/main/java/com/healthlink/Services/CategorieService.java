package com.healthlink.Services;

import com.healthlink.Entites.Categorie;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService {
    private Connection connection;
    private static final String TABLE_NAME = "categorie"; // Nom de table constant

    public CategorieService() {
        try {
            this.connection = MyDB.getInstance().getConnection();
            ensureTableExists(); // Vérifie que la table existe au démarrage
            ensureDefaultCategoryExists(); // Crée la catégorie par défaut si nécessaire
        } catch (SQLException e) {
            System.err.println("Erreur connexion DB: " + e.getMessage());
            throw new RuntimeException("Échec de l'initialisation du service", e);
        }
    }

    private void ensureTableExists() {
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, TABLE_NAME, null);

            if (!tables.next()) {
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate("CREATE TABLE " + TABLE_NAME + " (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "nom VARCHAR(255) NOT NULL)");
                    System.out.println("Table '" + TABLE_NAME + "' créée avec succès");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification table: " + e.getMessage());
            throw new RuntimeException("Impossible de vérifier/créer la table", e);
        }
    }

    // CREATE - Ajout d'une catégorie
    public boolean addCategorie(Categorie categorie) {
        String req = "INSERT INTO " + TABLE_NAME + " (nom) VALUES (?)";

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, categorie.getNom());

            if (pst.executeUpdate() > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        categorie.setId(keys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout catégorie: " + e.getMessage());
        }
        return false;
    }

    // READ - Récupère toutes les catégories triées par nom
    public List<Categorie> getAllCategories() {
        List<Categorie> categories = new ArrayList<>();
        String req = "SELECT * FROM " + TABLE_NAME + " ORDER BY nom";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                categories.add(new Categorie(
                        rs.getInt("id"),
                        rs.getString("nom")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture catégories: " + e.getMessage());
        }
        return categories;
    }

    // UPDATE - Met à jour une catégorie existante
    public boolean updateCategorie(Categorie categorie) {
        String req = "UPDATE " + TABLE_NAME + " SET nom = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, categorie.getNom());
            pst.setInt(2, categorie.getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour catégorie ID " + categorie.getId() + ": " + e.getMessage());
            return false;
        }
    }

    // DELETE - Supprime une catégorie
    public boolean deleteCategorie(int id) {
        if (id == 1) {
            System.err.println("Impossible de supprimer la catégorie par défaut (ID=1)");
            return false;
        }

        String req = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression catégorie ID " + id + ": " + e.getMessage());
            return false;
        }
    }

    // Récupère une catégorie par son ID
    public Categorie getCategorieById(int categorieId) {
        String req = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, categorieId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Categorie(
                            rs.getInt("id"),
                            rs.getString("nom")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération catégorie ID " + categorieId + ": " + e.getMessage());
        }
        return null;
    }

    // Vérifie si une catégorie existe
    public boolean categoryExists(int categoryId) {
        String req = "SELECT 1 FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, categoryId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification existence catégorie ID " + categoryId + ": " + e.getMessage());
            return false;
        }
    }

    // Garantit l'existence de la catégorie par défaut
    public void ensureDefaultCategoryExists() {
        if (!categoryExists(1)) {
            Categorie defaultCat = new Categorie(1, "Non classé");
            if (addCategorie(defaultCat)) {
                System.out.println("✅ Catégorie par défaut (ID=1) créée");
            } else {
                System.err.println("❌ Échec création catégorie par défaut");
            }
        }
    }

    // Méthode utilitaire pour vérifier la connexion
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}