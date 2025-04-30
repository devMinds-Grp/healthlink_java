package com.healthlink.Services;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entites.Role;
import com.healthlink.utils.MyDB;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


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
                "WHERE u.email = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("mot_de_passe");
                // Vérifier le mot de passe avec BCrypt
                if (BCrypt.checkpw(password, hashedPassword)) {
                    Utilisateur user = new Utilisateur();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setMot_de_passe(hashedPassword); // Stocker le hachage (optionnel)
                    user.setImageprofile(rs.getString("imageprofile"));
                    user.setStatut(rs.getString("statut"));
                    // Création et initialisation du rôle
                    Role role = new Role();
                    role.setId(rs.getInt("role_id"));
                    role.setNom(rs.getString("role_nom"));
                    user.setRole(role);
                    // Ajouter d'autres champs si nécessaire (num_tel, adresse, etc.)
                    return user;
                }
            }
            return null; // Email non trouvé ou mot de passe incorrect
        }
    }

    public String generateProfileImage(String initials) {
        int width = 200;
        int height = 200;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fond bleu clair
        g2d.setColor(new Color(100, 149, 237));
        g2d.fillRect(0, 0, width, height);

        // Texte blanc (initiales)
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        g2d.setColor(Color.WHITE);

        // Centrage du texte
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(initials)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();

        g2d.drawString(initials, x, y);
        g2d.dispose();

        // Dossier de sortie local (même que saveProfileImage)
        File dossierImages = new File("profile_images");
        if (!dossierImages.exists()) {
            dossierImages.mkdir();
        }

        // Nom unique
        String nomFichier = "profile_" + initials + "_" + System.currentTimeMillis() + ".png";
        File fichierDestination = new File(dossierImages, nomFichier);

        try {
            ImageIO.write(image, "png", fichierDestination);
            return nomFichier; // Retourne le nom de fichier (comme saveProfileImage)
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void addPatient(Utilisateur utilisateur) {
        String req = "INSERT INTO utilisateur (role_id, nom, prenom, email, mot_de_passe, num_tel, statut, adresse, speciality, categorie_soin, image, imageprofile, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            // Hacher le mot de passe
            String hashedPassword = BCrypt.hashpw(utilisateur.getMot_de_passe(), BCrypt.gensalt(13));

            pst.setInt(1, utilisateur.getRole().getId()); // Rôle du patient (3)
            pst.setString(2, utilisateur.getNom());
            pst.setString(3, utilisateur.getPrenom());
            pst.setString(4, utilisateur.getEmail());
            pst.setString(5, hashedPassword); // Utiliser le mot de passe haché
            pst.setInt(6, utilisateur.getNum_tel());
            pst.setString(7, "approuvé");
            pst.setString(8, "");
            pst.setString(9, "");
            pst.setString(10, "");
            pst.setInt(11, 0);
            if (utilisateur.getImageprofile() != null) {
                pst.setString(12, utilisateur.getImageprofile());
            } else {
                String initials = (utilisateur.getNom().substring(0, 1) + utilisateur.getPrenom().substring(0, 1)).toUpperCase();
                String generatedImagePath = generateProfileImage(initials);
                pst.setString(12, generatedImagePath);
            }
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

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            // Hacher le mot de passe
            String hashedPassword = BCrypt.hashpw(utilisateur.getMot_de_passe(), BCrypt.gensalt(13));

            pst.setInt(1, utilisateur.getRole().getId()); // Rôle du médecin (2)
            pst.setString(2, utilisateur.getNom());
            pst.setString(3, utilisateur.getPrenom());
            pst.setString(4, utilisateur.getEmail());
            pst.setString(5, hashedPassword); // Utiliser le mot de passe haché
            pst.setInt(6, utilisateur.getNum_tel());
            pst.setString(7, "en attente");
            pst.setString(8, utilisateur.getAdresse());
            pst.setString(9, utilisateur.getSpeciality());
            pst.setString(10, "");
            pst.setString(11, utilisateur.getImage());
            if (utilisateur.getImageprofile() != null) {
                pst.setString(12, utilisateur.getImageprofile());
            } else {
                String initials = (utilisateur.getNom().substring(0, 1) + utilisateur.getPrenom().substring(0, 1)).toUpperCase();
                String generatedImagePath = generateProfileImage(initials);
                pst.setString(12, generatedImagePath);
            }
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
            System.err.println("Erreur lors de l'ajout du médecin : " + e.getMessage());
        }
    }

    public void addSoignant(Utilisateur utilisateur) {
        String req = "INSERT INTO utilisateur (role_id, nom, prenom, email, mot_de_passe, num_tel, statut, adresse, speciality, categorie_soin, image, imageprofile, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            // Hacher le mot de passe
            String hashedPassword = BCrypt.hashpw(utilisateur.getMot_de_passe(), BCrypt.gensalt(13));

            pst.setInt(1, utilisateur.getRole().getId()); // Rôle du soignant (par exemple 4)
            pst.setString(2, utilisateur.getNom());
            pst.setString(3, utilisateur.getPrenom());
            pst.setString(4, utilisateur.getEmail());
            pst.setString(5, hashedPassword); // Utiliser le mot de passe haché
            pst.setInt(6, 0);
            pst.setString(7, "en attente");
            pst.setString(8, "");
            pst.setString(9, "");
            pst.setString(10, utilisateur.getCategorie_soin());
            pst.setString(11, utilisateur.getImage());
            if (utilisateur.getImageprofile() != null) {
                pst.setString(12, utilisateur.getImageprofile());
            } else {
                String initials = (utilisateur.getNom().substring(0, 1) + utilisateur.getPrenom().substring(0, 1)).toUpperCase();
                String generatedImagePath = generateProfileImage(initials);
                pst.setString(12, generatedImagePath);
            }
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
            System.err.println("Erreur lors de l'ajout du soignant : " + e.getMessage());
        }
    }

    public static void logout() {
        connectedUtilisateur = null;
    }

}
