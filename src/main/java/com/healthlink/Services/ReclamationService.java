package com.healthlink.Services;

import com.healthlink.Entites.Reclamation;
import com.healthlink.utils.MyDB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
            ensureImageColumnExists(); // Vérifie et ajoute la colonne image si nécessaire
        } catch (SQLException e) {
            System.err.println("Erreur DB: " + e.getMessage());
            throw new RuntimeException("Échec de l'initialisation du service", e);
        }
    }

    private void ensureImageColumnExists() {
        try {
            DatabaseMetaData dbMetaData = connection.getMetaData();
            ResultSet columns = dbMetaData.getColumns(null, null, "reclamation", "image");

            if (!columns.next()) {
                // La colonne n'existe pas, on l'ajoute
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate("ALTER TABLE reclamation ADD COLUMN image VARCHAR(255)");
                    System.out.println("Colonne 'image' ajoutée à la table reclamation");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification colonne image: " + e.getMessage());
        }
    }

    private boolean validateCategory(int categoryId) {
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

        String req = "INSERT INTO reclamation (titre, description, category_id, statut, image) VALUES (?, ?, ?, ?, ?)";
        try {
            // Gestion de l'image si elle existe
            String imagePath = null;
            if (r.getImage() != null && !r.getImage().isEmpty()) {
                String imageFileName = Paths.get(r.getImage()).getFileName().toString();
                Path imageDestination = Paths.get("src/main/resources/images/", imageFileName);
                Files.copy(Paths.get(r.getImage()), imageDestination, StandardCopyOption.REPLACE_EXISTING);
                imagePath = imageFileName;
            }

            try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, r.getTitre());
                pst.setString(2, r.getDescription());
                pst.setInt(3, r.getCategoryId());
                pst.setString(4, "En attente");
                pst.setString(5, imagePath);

                if (pst.executeUpdate() > 0) {
                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) {
                            r.setId(rs.getInt(1));
                            return true;
                        }
                    }
                }
            }
        } catch (IOException | SQLException e) {
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
                r.setImage(rs.getString("image"));
                r.setStatut(rs.getString("statut"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture réclamations: " + e.getMessage());
        }
        return list;
    }
    public boolean acceptReclamation(int id) {
        return updateReclamationStatus(id, "Acceptée");
    }

    public boolean rejectReclamation(int id) {
        return updateReclamationStatus(id, "Rejetée");
    }

    private boolean updateReclamationStatus(int id, String status) {
        if (!isConnectionValid()) {
            System.err.println("Erreur: Connexion DB invalide");
            return false;
        }

        String req = "UPDATE reclamation SET statut=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statut réclamation: " + e.getMessage());
        }
        return false;
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

        String req = "UPDATE reclamation SET titre=?, description=?, category_id=?, image=? WHERE id=?";
        try {
            // Gestion de l'image si elle existe
            String imagePath = null;
            if (r.getImage() != null && !r.getImage().isEmpty()) {
                String imageFileName = Paths.get(r.getImage()).getFileName().toString();
                Path imageDestination = Paths.get("src/main/resources/images/", imageFileName);
                Files.copy(Paths.get(r.getImage()), imageDestination, StandardCopyOption.REPLACE_EXISTING);
                imagePath = imageFileName;
            }

            try (PreparedStatement pst = connection.prepareStatement(req)) {
                pst.setString(1, r.getTitre());
                pst.setString(2, r.getDescription());
                pst.setInt(3, r.getCategoryId());
                pst.setString(4, imagePath);
                pst.setInt(5, r.getId());
                return pst.executeUpdate() > 0;
            }
        } catch (IOException | SQLException e) {
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