package com.healthlink.Services;

import com.healthlink.Entites.Reclamation;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {
    private Connection connection;
    private final CategorieService categorieService = new CategorieService();

    public ReclamationService() {
        try {
            this.connection = MyDB.getInstance().getConnection();
            ensureForeignKeyConstraint(); // Vérifie et crée les contraintes au démarrage
        } catch (SQLException e) {
            System.err.println("Erreur DB: " + e.getMessage());
            throw new RuntimeException("Échec de l'initialisation du service", e);
        }
    }
    private boolean validateCategory(int categoryId) {
        // Vérifie que la catégorie existe ET que l'ID n'est pas null
        return categoryId > 0 && categorieService.categoryExists(categoryId);
    }

    private boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void ensureForeignKeyConstraint() {
        try {
            // Vérifier que la table categorie existe
            if (!tableExists("categorie")) {
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate("CREATE TABLE categorie (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "nom VARCHAR(255) NOT NULL)");
                    System.out.println("Table 'categorie' créée avec succès");
                }
            }

            // Vérifier et recréer la contrainte
            try (Statement st = connection.createStatement()) {
                // Supprimer l'ancienne contrainte si elle existe
                st.execute("ALTER TABLE reclamation DROP FOREIGN KEY IF EXISTS FK_CE60640412469DE2");

                // Recréer la contrainte
                st.execute("ALTER TABLE reclamation " +
                        "ADD CONSTRAINT FK_CE60640412469DE2 " +
                        "FOREIGN KEY (category_id) REFERENCES categorie(id) " +
                        "ON DELETE RESTRICT ON UPDATE CASCADE");
                System.out.println("Contrainte de clé étrangère vérifiée/créée");
            }
        } catch (SQLException e) {
            System.err.println("Erreur contrainte clé étrangère: " + e.getMessage());
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, tableName, null);
        return tables.next();
    }

    public boolean addReclamation(Reclamation r) {
        if (!isConnectionValid()) {
            System.err.println("Erreur: Connexion DB invalide");
            return false;
        }

        // Validation des données
        if (r.getTitre() == null || r.getTitre().trim().isEmpty() ||
                r.getDescription() == null || r.getDescription().trim().isEmpty()) {
            System.err.println("Erreur: Titre ou description vide");
            return false;
        }

        // Vérification de la catégorie
        categorieService.ensureDefaultCategoryExists();
        if (!categorieService.categoryExists(r.getCategoryId())) {
            System.err.println("Erreur: Catégorie ID " + r.getCategoryId() + " inexistante");
            return false;
        }

        String req = "INSERT INTO reclamation (titre, description, category_id, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, r.getTitre());
            pst.setString(2, r.getDescription());
            pst.setInt(3, r.getCategoryId());
            pst.setString(4, "En attente");

            if (pst.executeUpdate() > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        r.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout réclamation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Reclamation> getAllReclamations() {
        List<Reclamation> list = new ArrayList<>();
        if (!isConnectionValid()) {
            System.err.println("Erreur: Connexion DB invalide");
            return list;
        }

        String req = "SELECT * FROM reclamation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getInt("category_id")
                );
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture réclamations: " + e.getMessage());
        }
        return list;
    }

    public boolean updateReclamation(Reclamation r) {
        if (!isConnectionValid()) {
            System.err.println("Erreur: Connexion DB invalide");
            return false;
        }

        // Validation des données
        if (r.getTitre() == null || r.getTitre().trim().isEmpty() ||
                r.getDescription() == null || r.getDescription().trim().isEmpty()) {
            System.err.println("Erreur: Titre ou description vide");
            return false;
        }

        // Vérification de la catégorie
        if (!categorieService.categoryExists(r.getCategoryId())) {
            System.err.println("Erreur: Catégorie ID " + r.getCategoryId() + " inexistante");
            return false;
        }

        String req = "UPDATE reclamation SET titre=?, description=?, category_id=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, r.getTitre());
            pst.setString(2, r.getDescription());
            pst.setInt(3, r.getCategoryId());
            pst.setInt(4, r.getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour réclamation: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteReclamation(int id) {
        if (!isConnectionValid()) {
            System.err.println("Erreur: Connexion DB invalide");
            return false;
        }

        String req = "DELETE FROM reclamation WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression réclamation: " + e.getMessage());
        }
        return false;
    }
}