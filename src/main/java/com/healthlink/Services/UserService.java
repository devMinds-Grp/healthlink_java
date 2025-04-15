package com.healthlink.Services;

import com.healthlink.Entites.User;
import com.healthlink.Entites.Role;
import com.healthlink.Interfaces.InterfaceCRUD;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements InterfaceCRUD<User> {
    Connection conn;

    public UserService() {

        conn = MyDB.getInstance().getCon();
    }

    //Ft pour patient
    
    public void addPatient(User user) {
        String req = "INSERT INTO user (role_id, nom, prenom, email, mot_de_passe, num_tel,statut,adresse,speciality,categorie_soin,image,imageprofile,reset_code ) " +
                "VALUES (?, ?, ?, ?, ?, ?,?, ?,?,?,?,?,?)";

        try (PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, user.getRole().getId()); // Rôle du patient (3)
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getMot_de_passe());
            pst.setInt(6, user.getNum_tel());
            pst.setString(7, "approuvé");
            pst.setString(8, "");
            pst.setString(9, "");
            pst.setString(10, "");
            pst.setInt(11, 0);
            pst.setString(12, user.getImageprofile());
            pst.setInt(13, 0);


            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du patient : " + e.getMessage());
        }
    }
    
    public List<User> findAllPatients() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 3";
        List<User> patients = new ArrayList<>();

        try (PreparedStatement pst = conn.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des patients: " + e.getMessage());
        }
        return patients;
    }
    private User extractPatientFromResultSet(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("role_id"));
        role.setNom(rs.getString("role_nom"));

        User user = new User();
        user.setId(rs.getInt("id"));
        user.setRole(role);
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setNum_tel(rs.getInt("num_tel"));
        // On ne récupère que les champs nécessaires pour l'affichage

        return user;
    }
    public User findPatientById(int id) {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.id = ? AND u.role_id = 3";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return extractPatientFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du patient: " + e.getMessage());
        }
        return null;
    }


    // Méthode pour supprimer un patient
    public void deletePatient(int id) {
        String req = "DELETE FROM user WHERE id = ? AND role_id = 3";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("Aucun patient trouvé avec cet ID ou ce n'est pas un patient");
            } else {
                System.out.println("Patient supprimé avec succès");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du patient: " + e.getMessage());
        }
    }

    // Méthode pour mettre à jour un patient
    public void updatePatient(User patient) {
        String req = "UPDATE user SET nom = ?, prenom = ?, email = ?, num_tel = ? WHERE id = ? AND role_id = 3";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, patient.getNom());
            pst.setString(2, patient.getPrenom());
            pst.setString(3, patient.getEmail());
            pst.setInt(4, patient.getNum_tel());
            pst.setInt(5, patient.getId());

            int affectedRows = pst.executeUpdate();
            System.out.println(affectedRows + " ligne(s) mise(s) à jour");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du patient: " + e.getMessage());
        }
    }



    //Ft medecin
    
    public void addMedecin(User user) {
        String req = "INSERT INTO user (role_id, nom, prenom, email, mot_de_passe, num_tel, statut, adresse, speciality, categorie_soin, image, imageprofile, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, user.getRole().getId()); // Rôle du médecin (2)
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getMot_de_passe());
            pst.setInt(6, user.getNum_tel());
            pst.setString(7, "approuvé");
            pst.setString(8, user.getAdresse());
            pst.setString(9, user.getSpeciality()); // Spécialité du médecin
            pst.setString(10, ""); // Catégorie de soin (peut être vide ou définie)
            pst.setString(11, user.getImage()); // Image par défaut
            pst.setString(12, user.getImageprofile()); // Image profile par défaut
            pst.setInt(13, 0); // Reset code par défaut

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du médecin : " + e.getMessage());
        }
    }
    
    public List<User> findAllMedecins() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 2";
        List<User> medecins = new ArrayList<>();

        try (PreparedStatement pst = conn.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                medecins.add(extractMedecinFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des médecins: " + e.getMessage());
        }
        return medecins;
    }

    private User extractMedecinFromResultSet(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("role_id"));
        role.setNom(rs.getString("role_nom"));

        User medecin = new User();
        medecin.setId(rs.getInt("id"));
        medecin.setRole(role);
        medecin.setNom(rs.getString("nom"));
        medecin.setPrenom(rs.getString("prenom"));
        medecin.setEmail(rs.getString("email"));
        medecin.setNum_tel(rs.getInt("num_tel"));
        medecin.setAdresse(rs.getString("adresse"));
        medecin.setSpeciality(rs.getString("speciality"));

        return medecin;
    }
    // Méthode pour récupérer un médecin par ID
    public User findMedecinById(int id) {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.id = ? AND u.role_id = 2";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return extractMedecinFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du médecin: " + e.getMessage());
        }
        return null;
    }

    // Méthode pour supprimer un médecin
    public void deleteMedecin(int id) {
        String req = "DELETE FROM user WHERE id = ? AND role_id = 2";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("Aucun médecin trouvé avec cet ID ou ce n'est pas un médecin");
            } else {
                System.out.println("Médecin supprimé avec succès");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du médecin: " + e.getMessage());
        }
    }
    // Méthode pour mettre à jour un médecin
    public void updateMedecin(User medecin) {
        String req = "UPDATE user SET nom = ?, prenom = ?, email = ?, num_tel = ?, speciality = ?, adresse = ? WHERE id = ? AND role_id = 2";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, medecin.getNom());
            pst.setString(2, medecin.getPrenom());
            pst.setString(3, medecin.getEmail());
            pst.setInt(4, medecin.getNum_tel());
            pst.setString(5, medecin.getSpeciality());
            pst.setString(6, medecin.getAdresse());
            pst.setInt(7, medecin.getId());

            int affectedRows = pst.executeUpdate();
            System.out.println(affectedRows + " ligne(s) mise(s) à jour");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du médecin: " + e.getMessage());
        }
    }


    //Ft soignant
    
    public void addSoignant(User user) {
        String req = "INSERT INTO user (role_id, nom, prenom, email, mot_de_passe, num_tel, statut, adresse, speciality, categorie_soin, image, imageprofile, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, user.getRole().getId()); // Rôle du soignant (à définir, par exemple 4)
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getMot_de_passe());
            pst.setInt(6, 0);
            pst.setString(7, "approuvé"); // Statut par défaut
            pst.setString(8, "");
            pst.setString(9, ""); // Spécialité si applicable
            pst.setString(10, user.getCategorie_soin()); // Catégorie de soin
            pst.setString(11, user.getImage()); // Image par défaut
            pst.setString(12, user.getImageprofile()); // Image profile par défaut
            pst.setInt(13, 0); // Reset code par défaut

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du soignant : " + e.getMessage());
        }
    }

    
    public List<User> findAllSoignants() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 4";
        List<User> soignants = new ArrayList<>();

        try (PreparedStatement pst = conn.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                soignants.add(extractSoignantFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des soignants: " + e.getMessage());
        }
        return soignants;
    }

    private User extractSoignantFromResultSet(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("role_id"));
        role.setNom(rs.getString("role_nom"));

        User soignant = new User();
        soignant.setId(rs.getInt("id"));
        soignant.setRole(role);
        soignant.setNom(rs.getString("nom"));
        soignant.setPrenom(rs.getString("prenom"));
        soignant.setEmail(rs.getString("email"));
        soignant.setCategorie_soin(rs.getString("categorie_soin"));

        return soignant;
    }

    public void deleteSoignant(int id) {
        String req = "DELETE FROM user WHERE id = ? AND role_id = 4";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Soignant supprimé avec succès.");
            } else {
                System.out.println("Aucun soignant trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du soignant : " + e.getMessage());
        }
    }

    public void updateSoignant(User soignant) {
        String req = "UPDATE user SET " +
                "nom = ?, prenom = ?, email = ?, categorie_soin = ?, image = ?, imageprofile = ? " +
                "WHERE id = ? AND role_id = 4";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, soignant.getNom());
            pst.setString(2, soignant.getPrenom());
            pst.setString(3, soignant.getEmail());
            pst.setString(4, soignant.getCategorie_soin());
            pst.setString(5, soignant.getImage());
            pst.setString(6, soignant.getImageprofile());
            pst.setInt(7, soignant.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Soignant mis à jour avec succès !");
            } else {
                System.out.println("Aucun soignant trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du soignant : " + e.getMessage());
        }
    }

    // Méthode supplémentaire pour trouver un utilisateur par ID
    public User findById(int id) {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.id = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Création du Role
                    Role role = new Role();
                    role.setId(rs.getInt("role_id"));
                    role.setNom(rs.getString("role_nom"));

                    // Création du User avec tous les champs
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setRole(role);
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setMot_de_passe(rs.getString("mot_de_passe"));
                    user.setNum_tel(rs.getInt("num_tel"));
                    user.setAdresse(rs.getString("adresse"));
                    user.setSpeciality(rs.getString("speciality"));
                    user.setCategorie_soin(rs.getString("categorie_soin"));
                    user.setImage(rs.getString("image"));
                    user.setImageprofile(rs.getString("imageprofile"));
                    user.setStatut(rs.getString("statut"));
                    user.setReset_code(rs.getInt("reset_code"));

                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur : " + e.getMessage());
        }
        return null;
    }
    
    public void add(User user) {
        String req = "INSERT INTO user (role_id, nom, prenom, email, mot_de_passe, num_tel, " +
                "adresse, speciality, categorie_soin, image, imageprofile, statut, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // 13 paramètres

        try (PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, user.getRole().getId());
            pst.setString(2, user.getNom());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getEmail());
            pst.setString(5, user.getMot_de_passe());
            pst.setInt(6, user.getNum_tel());
            pst.setString(7, user.getAdresse());
            pst.setString(8, user.getSpeciality());
            pst.setString(9, user.getCategorie_soin());
            pst.setString(10, user.getImage());
            pst.setString(11, user.getImageprofile());
            pst.setString(12, user.getStatut());
            pst.setInt(13, user.getReset_code());

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }

            System.out.println("Utilisateur ajouté avec succès ! ID: " + user.getId());
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
            e.printStackTrace(); // Affichez la stack trace complète pour le débogage
        }
    }
    
    public void update(User user) {
        String req = "UPDATE user SET " +
                "role_id = ?, " +
                "nom = ?, " +
                "prenom = ?, " +
                "email = ?, " +
                "mot_de_passe = ?, " +
                "num_tel = ?, " +
                "adresse = ?, " +
                "speciality = ?, " +
                "categorie_soin = ?, " +
                "image = ?, " +
                "imageprofile = ?, " +
                "statut = ?, " +
                "reset_code = ? " +
                "WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            int i = 1;
            pst.setInt(i++, user.getRole() != null ? user.getRole().getId() : null);
            pst.setString(i++, user.getNom());
            pst.setString(i++, user.getPrenom());
            pst.setString(i++, user.getEmail());
            pst.setString(i++, user.getMot_de_passe());
            pst.setInt(i++, user.getNum_tel());
            pst.setString(i++, user.getAdresse());
            pst.setString(i++, user.getSpeciality());
            pst.setString(i++, user.getCategorie_soin());
            pst.setString(i++, user.getImage());
            pst.setString(i++, user.getImageprofile());
            pst.setString(i++, user.getStatut());
            pst.setInt(i++, user.getReset_code());
            pst.setInt(i++, user.getId());

            int affectedRows = pst.executeUpdate();
            System.out.println(affectedRows + " ligne(s) mise(s) à jour");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

    
    public void delete(User user) {
        String req = "DELETE FROM `user` WHERE `id` = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, user.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public List<User> find() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id";
        List<User> users = new ArrayList<>();

        try (PreparedStatement pst = conn.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                // Création du Role
                Role role = new Role();
                role.setId(rs.getInt("role_id"));
                role.setNom(rs.getString("role_nom"));

                // Création du User avec tous les champs
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setRole(role);
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMot_de_passe(rs.getString("mot_de_passe"));
                user.setNum_tel(rs.getInt("num_tel"));
                user.setAdresse(rs.getString("adresse"));
                user.setSpeciality(rs.getString("speciality"));
                user.setCategorie_soin(rs.getString("categorie_soin"));
                user.setImage(rs.getString("image"));
                user.setImageprofile(rs.getString("imageprofile"));
                user.setStatut(rs.getString("statut"));
                user.setReset_code(rs.getInt("reset_code"));

                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }
        return users;
    }


}