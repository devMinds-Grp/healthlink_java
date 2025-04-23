package com.healthlink.Services;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entites.Role;
import com.healthlink.utils.MyDB;

import java.sql.*;

public class AuthService  {
    private static Utilisateur connectedUtilisateur;

    public static void setConnectedUtilisateur(Utilisateur utilisateur) {
        connectedUtilisateur = utilisateur;
    }

    public static Utilisateur getConnectedUtilisateur() {
        return connectedUtilisateur;
    }
    private Connection connection;

    public AuthService() {
        try {
            this.connection = MyDB.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("La connexion à la base de données a échoué");
            }
            System.out.println("PrescriptionService: Database connection established");
        } catch (SQLException e) {
            System.err.println("Erreur critique de connexion à la base: " + e.getMessage());
            throw new RuntimeException("Échec d'initialisation du PrescriptionService", e);
        }
    }
    public Utilisateur login(String email, String password) throws SQLException {
        String query = "SELECT u.*, r.id as role_id, r.nom as role_nom " +
                "FROM utilisateur u " +
                "JOIN role r ON u.role_id = r.id " +
                "WHERE u.email = ? AND u.mot_de_passe = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, email);
        ps.setString(2, password);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Utilisateur user = new Utilisateur();
            user.setId(rs.getInt("id"));
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setEmail(rs.getString("email"));
            user.setMot_de_passe(rs.getString("mot_de_passe"));
            user.setImageprofile(rs.getString("imageprofile"));
            // Création et initialisation du rôle
            Role role = new Role();
            role.setId(rs.getInt("role_id"));
            role.setNom(rs.getString("role_nom"));
            user.setRole(role);
            user.setStatut(rs.getString("statut"));


            // Remplis d'autres champs si nécessaire
            return user;
        } else {
            return null;
        }
    }
    public void addPatient(Utilisateur utilisateur) {
        String req = "INSERT INTO utilisateur (role_id, nom, prenom, email, mot_de_passe, num_tel,statut,adresse,speciality,categorie_soin,image,imageprofile,reset_code ) " +
                "VALUES (?, ?, ?, ?, ?, ?,?, ?,?,?,?,?,?)";

        try (PreparedStatement pst =  connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, utilisateur.getRole().getId()); // Rôle du patient (3)
            pst.setString(2, utilisateur.getNom());
            pst.setString(3, utilisateur.getPrenom());
            pst.setString(4, utilisateur.getEmail());
            pst.setString(5, utilisateur.getMot_de_passe());
            pst.setInt(6, utilisateur.getNum_tel());
            pst.setString(7, "approuvé");
            pst.setString(8, "");
            pst.setString(9, "");
            pst.setString(10, "");
            pst.setInt(11, 0);
            pst.setString(12, utilisateur.getImageprofile());
            pst.setInt(13, 0);


            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        utilisateur.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du patient : " + e.getMessage());
        }
    }
    public void addMedecin(Utilisateur utilisateur) {
        String req = "INSERT INTO utilisateur (role_id, nom, prenom, email, mot_de_passe, num_tel, statut, adresse, speciality, categorie_soin, image, imageprofile, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst =  connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, utilisateur.getRole().getId()); // Rôle du médecin (2)
            pst.setString(2, utilisateur.getNom());
            pst.setString(3, utilisateur.getPrenom());
            pst.setString(4, utilisateur.getEmail());
            pst.setString(5, utilisateur.getMot_de_passe());
            pst.setInt(6, utilisateur.getNum_tel());
            pst.setString(7, "en attente");
            pst.setString(8, utilisateur.getAdresse());
            pst.setString(9, utilisateur.getSpeciality()); // Spécialité du médecin
            pst.setString(10, ""); // Catégorie de soin (peut être vide ou définie)
            pst.setString(11, utilisateur.getImage()); // Image par défaut
            pst.setString(12, utilisateur.getImageprofile()); // Image profile par défaut
            pst.setInt(13, 0); // Reset code par défaut

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        utilisateur.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du médecin : " + e.getMessage());
        }
    }
    public void addSoignant(Utilisateur utilisateur) {
        String req = "INSERT INTO utilisateur (role_id, nom, prenom, email, mot_de_passe, num_tel, statut, adresse, speciality, categorie_soin, image, imageprofile, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst =  connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, utilisateur.getRole().getId()); // Rôle du soignant (à définir, par exemple 4)
            pst.setString(2, utilisateur.getNom());
            pst.setString(3, utilisateur.getPrenom());
            pst.setString(4, utilisateur.getEmail());
            pst.setString(5, utilisateur.getMot_de_passe());
            pst.setInt(6, 0);
            pst.setString(7, "en attente"); // Statut par défaut
            pst.setString(8, "");
            pst.setString(9, ""); // Spécialité si applicable
            pst.setString(10, utilisateur.getCategorie_soin()); // Catégorie de soin
            pst.setString(11, utilisateur.getImage()); // Image par défaut
            pst.setString(12, utilisateur.getImageprofile()); // Image profile par défaut
            pst.setInt(13, 0); // Reset code par défaut

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        utilisateur.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du soignant : " + e.getMessage());
        }
    }

    public static void logout() {
        connectedUtilisateur = null;
    }

}
