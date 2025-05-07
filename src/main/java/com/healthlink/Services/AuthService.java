package com.healthlink.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entites.Role;
import com.healthlink.utils.MyDB;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


import java.sql.*;
import java.util.ArrayList;

import static java.lang.System.out;

public class AuthService  {

    private static Utilisateur connectedUtilisateur;

    public static void setConnectedUtilisateur(Utilisateur utilisateur) {
        connectedUtilisateur = utilisateur;
    }

    public static Utilisateur getConnectedUtilisateur() {
        return connectedUtilisateur;
    }
    private Connection connection;

    private static final String TRAIN_API_URL = "http://localhost:5000/train";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

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


    public int addPatient(Utilisateur utilisateur) throws SQLException {
        // Vérifier si l'email existe déjà
        String checkEmailQuery = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkEmailQuery)) {
            checkStmt.setString(1, utilisateur.getEmail());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("L'email " + utilisateur.getEmail() + " est déjà utilisé.");
            }
        }

        String req = "INSERT INTO utilisateur (role_id, nom, prenom, email, mot_de_passe, num_tel, statut, adresse, speciality, categorie_soin, image, imageprofile, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            String hashedPassword = BCrypt.hashpw(utilisateur.getMot_de_passe(), BCrypt.gensalt(13));

            pst.setInt(1, utilisateur.getRole().getId());
            pst.setString(2, utilisateur.getNom());
            pst.setString(3, utilisateur.getPrenom());
            pst.setString(4, utilisateur.getEmail());
            pst.setString(5, hashedPassword);
            pst.setInt(6, utilisateur.getNum_tel());
            pst.setString(7, "approuvé");
            pst.setString(8, "");
            pst.setString(9, "");
            pst.setString(10, "");
            pst.setInt(11, 0);
            pst.setString(12, utilisateur.getImageprofile() != null ? utilisateur.getImageprofile() : generateProfileImage(
                    (utilisateur.getNom().substring(0, 1) + utilisateur.getPrenom().substring(0, 1)).toUpperCase()));
            pst.setInt(13, 0);

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return -1;
        }
    }
    public boolean registerUserImage(int userId, String base64Image) throws IOException, InterruptedException {
        String jsonBody = String.format("{\"user_id\": %d, \"image\": \"%s\"}", userId, base64Image);
        out.println("Envoi à l'URL : http://localhost:5000/train");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/train"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        out.println("Réponse Flask /train : " + response.body() + " (Statut : " + response.statusCode() + ")");
        if (response.statusCode() >= 400) {
            throw new IOException("Échec de la requête : " + response.statusCode());
        }

        // Parser manuellement la réponse JSON
        String responseBody = response.body();
        if (responseBody.contains("\"status\":\"success\"")) {
            return true;
        }
        return false;
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
//            if (utilisateur.getImageprofile() != null) {
//                pst.setString(12, utilisateur.getImageprofile());
//            } else {
//                String initials = (utilisateur.getNom().substring(0, 1) + utilisateur.getPrenom().substring(0, 1)).toUpperCase();
//                String generatedImagePath = generateProfileImage(initials);
//                pst.setString(12, generatedImagePath);
//            }
            if (utilisateur.getImageprofile() != null) {
                pst.setString(12, utilisateur.getImageprofile());
            } else {
                String initials = (utilisateur.getNom().substring(0, 1) + utilisateur.getPrenom().substring(0, 1)).toUpperCase();
                String generatedImagePath = generateProfileImage(initials);
                if (generatedImagePath == null) {
                    throw new RuntimeException("Échec de la génération de l'image de profil par défaut.");
                }
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
//            if (utilisateur.getImageprofile() != null) {
//                pst.setString(12, utilisateur.getImageprofile());
//            } else {
//                String initials = (utilisateur.getNom().substring(0, 1) + utilisateur.getPrenom().substring(0, 1)).toUpperCase();
//                String generatedImagePath = generateProfileImage(initials);
//                pst.setString(12, generatedImagePath);
//            }
            if (utilisateur.getImageprofile() != null) {
                pst.setString(12, utilisateur.getImageprofile());
            } else {
                String initials = (utilisateur.getNom().substring(0, 1) + utilisateur.getPrenom().substring(0, 1)).toUpperCase();
                String generatedImagePath = generateProfileImage(initials);
                if (generatedImagePath == null) {
                    throw new RuntimeException("Échec de la génération de l'image de profil par défaut.");
                }
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

    public  Utilisateur findUserByImageProfile(String imageProfile) throws SQLException {
        String query = "SELECT u.*, r.id as role_id, r.nom as role_nom " +
                "FROM utilisateur u " +
                "JOIN role r ON u.role_id = r.id " +
                "WHERE u.imageprofile = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, imageProfile);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMot_de_passe(rs.getString("mot_de_passe"));
                user.setImageprofile(rs.getString("imageprofile"));
                user.setStatut(rs.getString("statut"));

                Role role = new Role();
                role.setId(rs.getInt("role_id"));
                role.setNom(rs.getString("role_nom"));
                user.setRole(role);

                return user;
            }
            return null; // Aucun utilisateur trouvé avec cette image de profil
        }
    }

    //    public  List<Utilisateur> getAllUsersWithImageProfile() throws SQLException {
//        List<Utilisateur> users = new ArrayList<>();
//        String query = "SELECT u.*, r.id as role_id, r.nom as role_nom " +
//                "FROM utilisateur u " +
//                "JOIN role r ON u.role_id = r.id " +
//                "WHERE u.imageprofile IS NOT NULL";
//
//        try (PreparedStatement ps = connection.prepareStatement(query)) {
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                Utilisateur user = new Utilisateur();
//                user.setId(rs.getInt("id"));
//                user.setNom(rs.getString("nom"));
//                user.setPrenom(rs.getString("prenom"));
//                user.setEmail(rs.getString("email"));
//                user.setMot_de_passe(rs.getString("mot_de_passe"));
//                user.setImageprofile(rs.getString("imageprofile"));
//                user.setStatut(rs.getString("statut"));
//
//                Role role = new Role();
//                role.setId(rs.getInt("role_id"));
//                role.setNom(rs.getString("role_nom"));
//                user.setRole(role);
//
//                users.add(user);
//            }
//        }
//        return users;
//    }
    public List<Utilisateur> getAllUsersWithImageProfile() throws SQLException {
        List<Utilisateur> users = new ArrayList<>();
        String query = "SELECT u.*, r.id as role_id, r.nom as role_nom " +
                "FROM utilisateur u " +
                "JOIN role r ON u.role_id = r.id " +
                "WHERE u.imageprofile IS NOT NULL";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMot_de_passe(rs.getString("mot_de_passe"));
                user.setImageprofile(rs.getString("imageprofile"));
                user.setStatut(rs.getString("statut"));

                Role role = new Role();
                role.setId(rs.getInt("role_id"));
                role.setNom(rs.getString("role_nom"));
                user.setRole(role);

                users.add(user);
            }
        }
        return users;
    }

}
