package com.healthlink.Services;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entites.Role;
import com.healthlink.Interfaces.InterfaceCRUD;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements InterfaceCRUD<Utilisateur> {
    private Connection connection;

    public UserService() {
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

    //Ft pour patient
    
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
    
    public List<Utilisateur> findAllPatients() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM utilisateur u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 3";
        List<Utilisateur> patients = new ArrayList<>();

        try (PreparedStatement pst =  connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des patients: " + e.getMessage());
        }
        return patients;
    }
    private Utilisateur extractPatientFromResultSet(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("role_id"));
        role.setNom(rs.getString("role_nom"));

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getInt("id"));
        utilisateur.setRole(role);
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setEmail(rs.getString("email"));
        utilisateur.setNum_tel(rs.getInt("num_tel"));
        // On ne récupère que les champs nécessaires pour l'affichage

        return utilisateur;
    }
    public Utilisateur findPatientById(int id) {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM utilisateur u LEFT JOIN role r ON u.role_id = r.id WHERE u.id = ? AND u.role_id = 3";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
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
        String req = "DELETE FROM utilisateur WHERE id = ? AND role_id = 3";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
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
    public void updatePatient(Utilisateur patient) {
        String req = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, num_tel = ?,mot_de_passe = ?, imageprofile = ?  WHERE id = ? AND role_id = 3";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
            pst.setString(1, patient.getNom());
            pst.setString(2, patient.getPrenom());
            pst.setString(3, patient.getEmail());
            pst.setInt(4, patient.getNum_tel());
            pst.setString(5, patient.getMot_de_passe());
            pst.setString(6, patient.getImageprofile());
            pst.setInt(7, patient.getId());

            int affectedRows = pst.executeUpdate();
            System.out.println(affectedRows + " ligne(s) mise(s) à jour");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du patient: " + e.getMessage());
        }
    }



    //Ft medecin
    
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
            pst.setString(7, "approuvé");
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
    
    public List<Utilisateur> findAllMedecins() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM utilisateur u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 2";
        List<Utilisateur> medecins = new ArrayList<>();

        try (PreparedStatement pst =  connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                medecins.add(extractMedecinFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des médecins: " + e.getMessage());
        }
        return medecins;
    }

    private Utilisateur extractMedecinFromResultSet(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("role_id"));
        role.setNom(rs.getString("role_nom"));

        Utilisateur medecin = new Utilisateur();
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
    public Utilisateur findMedecinById(int id) {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM utilisateur u LEFT JOIN role r ON u.role_id = r.id WHERE u.id = ? AND u.role_id = 2";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
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
        String req = "DELETE FROM utilisateur WHERE id = ? AND role_id = 2";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
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
    public void updateMedecin(Utilisateur medecin) {
        String req = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, num_tel = ?, speciality = ?, adresse = ?,imageprofile =? ,image=?,mot_de_passe=? WHERE id = ? AND role_id = 2";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
            pst.setString(1, medecin.getNom());
            pst.setString(2, medecin.getPrenom());
            pst.setString(3, medecin.getEmail());
            pst.setInt(4, medecin.getNum_tel());
            pst.setString(5, medecin.getSpeciality());
            pst.setString(6, medecin.getAdresse());
            pst.setString(7, medecin.getImageprofile());
            pst.setString(8, medecin.getImage());
            pst.setString(9, medecin.getMot_de_passe());
            pst.setInt(10, medecin.getId());

            int affectedRows = pst.executeUpdate();
            System.out.println(affectedRows + " ligne(s) mise(s) à jour");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du médecin: " + e.getMessage());
        }
    }


    //Ftnant
    
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
            pst.setString(7, "approuvé"); // Statut par défaut
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

    
    public List<Utilisateur> findAllSoignants() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM utilisateur u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 4";
        List<Utilisateur> soignants = new ArrayList<>();

        try (PreparedStatement pst =  connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                soignants.add(extractSoignantFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des soignants: " + e.getMessage());
        }
        return soignants;
    }

    private Utilisateur extractSoignantFromResultSet(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("role_id"));
        role.setNom(rs.getString("role_nom"));

        Utilisateur soignant = new Utilisateur();
        soignant.setId(rs.getInt("id"));
        soignant.setRole(role);
        soignant.setNom(rs.getString("nom"));
        soignant.setPrenom(rs.getString("prenom"));
        soignant.setEmail(rs.getString("email"));
        soignant.setCategorie_soin(rs.getString("categorie_soin"));

        return soignant;
    }

    public void deleteSoignant(int id) {
        String req = "DELETE FROM utilisateur WHERE id = ? AND role_id = 4";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
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

    public void updateSoignant(Utilisateur soignant) {
        String req = "UPDATE utilisateur SET " +
                "nom = ?, prenom = ?, email = ?, categorie_soin = ?,imageprofile =? ,image=?,mot_de_passe=?" +
                "WHERE id = ? AND role_id = 4";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
            pst.setString(1, soignant.getNom());
            pst.setString(2, soignant.getPrenom());
            pst.setString(3, soignant.getEmail());
            pst.setString(4, soignant.getCategorie_soin());
            pst.setString(5, soignant.getImageprofile());
            pst.setString(6, soignant.getImage());
            pst.setString(7, soignant.getMot_de_passe());
            pst.setInt(8, soignant.getId());

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
    public Utilisateur findById(int id) {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM utilisateur u LEFT JOIN role r ON u.role_id = r.id WHERE u.id = ?";

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Création du Role
                    Role role = new Role();
                    role.setId(rs.getInt("role_id"));
                    role.setNom(rs.getString("role_nom"));

                    // Création du User avec tous les champs
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId(rs.getInt("id"));
                    utilisateur.setRole(role);
                    utilisateur.setNom(rs.getString("nom"));
                    utilisateur.setPrenom(rs.getString("prenom"));
                    utilisateur.setEmail(rs.getString("email"));
                    utilisateur.setMot_de_passe(rs.getString("mot_de_passe"));
                    utilisateur.setNum_tel(rs.getInt("num_tel"));
                    utilisateur.setAdresse(rs.getString("adresse"));
                    utilisateur.setSpeciality(rs.getString("speciality"));
                    utilisateur.setCategorie_soin(rs.getString("categorie_soin"));
                    utilisateur.setImage(rs.getString("image"));
                    utilisateur.setImageprofile(rs.getString("imageprofile"));
                    utilisateur.setStatut(rs.getString("statut"));
                    utilisateur.setReset_code(rs.getInt("reset_code"));

                    return utilisateur;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur : " + e.getMessage());
        }
        return null;
    }
    
    public void add(Utilisateur utilisateur) {
        String req = "INSERT INTO utilisateur (role_id, nom, prenom, email, mot_de_passe, num_tel, " +
                "adresse, speciality, categorie_soin, image, imageprofile, statut, reset_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // 13 paramètres

        try (PreparedStatement pst =  connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, utilisateur.getRole().getId());
            pst.setString(2, utilisateur.getNom());
            pst.setString(3, utilisateur.getPrenom());
            pst.setString(4, utilisateur.getEmail());
            pst.setString(5, utilisateur.getMot_de_passe());
            pst.setInt(6, utilisateur.getNum_tel());
            pst.setString(7, utilisateur.getAdresse());
            pst.setString(8, utilisateur.getSpeciality());
            pst.setString(9, utilisateur.getCategorie_soin());
            pst.setString(10, utilisateur.getImage());
            pst.setString(11, utilisateur.getImageprofile());
            pst.setString(12, utilisateur.getStatut());
            pst.setInt(13, utilisateur.getReset_code());

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        utilisateur.setId(rs.getInt(1));
                    }
                }
            }

            System.out.println("Utilisateur ajouté avec succès ! ID: " + utilisateur.getId());
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
            e.printStackTrace(); // Affichez la stack trace complète pour le débogage
        }
    }
    
    public void update(Utilisateur utilisateur) {
        String req = "UPDATE utilisateur SET " +
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

        try (PreparedStatement pst =  connection.prepareStatement(req)) {
            int i = 1;
            pst.setInt(i++, utilisateur.getRole() != null ? utilisateur.getRole().getId() : null);
            pst.setString(i++, utilisateur.getNom());
            pst.setString(i++, utilisateur.getPrenom());
            pst.setString(i++, utilisateur.getEmail());
            pst.setString(i++, utilisateur.getMot_de_passe());
            pst.setInt(i++, utilisateur.getNum_tel());
            pst.setString(i++, utilisateur.getAdresse());
            pst.setString(i++, utilisateur.getSpeciality());
            pst.setString(i++, utilisateur.getCategorie_soin());
            pst.setString(i++, utilisateur.getImage());
            pst.setString(i++, utilisateur.getImageprofile());
            pst.setString(i++, utilisateur.getStatut());
            pst.setInt(i++, utilisateur.getReset_code());
            pst.setInt(i++, utilisateur.getId());

            int affectedRows = pst.executeUpdate();
            System.out.println(affectedRows + " ligne(s) mise(s) à jour");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

    
    public void delete(Utilisateur utilisateur) {
        String req = "DELETE FROM `utilisateur` WHERE `id` = ?";
        try (PreparedStatement pst =  connection.prepareStatement(req)) {
            pst.setInt(1, utilisateur.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public List<Utilisateur> find() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM utilisateur u LEFT JOIN role r ON u.role_id = r.id";
        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (PreparedStatement pst =  connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                // Création du Role
                Role role = new Role();
                role.setId(rs.getInt("role_id"));
                role.setNom(rs.getString("role_nom"));

                // Création du User avec tous les champs
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setRole(role);
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setPrenom(rs.getString("prenom"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setMot_de_passe(rs.getString("mot_de_passe"));
                utilisateur.setNum_tel(rs.getInt("num_tel"));
                utilisateur.setAdresse(rs.getString("adresse"));
                utilisateur.setSpeciality(rs.getString("speciality"));
                utilisateur.setCategorie_soin(rs.getString("categorie_soin"));
                utilisateur.setImage(rs.getString("image"));
                utilisateur.setImageprofile(rs.getString("imageprofile"));
                utilisateur.setStatut(rs.getString("statut"));
                utilisateur.setReset_code(rs.getInt("reset_code"));

                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }
        return utilisateurs;
    }
    public List<Utilisateur> findEnAttente() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom " +
                "FROM utilisateur u " +
                "LEFT JOIN role r ON u.role_id = r.id " +
                "WHERE u.statut = 'en attente'";

        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                // Création du Role
                Role role = new Role();
                role.setId(rs.getInt("role_id"));
                role.setNom(rs.getString("role_nom"));

                // Création du User
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setRole(role);
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setPrenom(rs.getString("prenom"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setMot_de_passe(rs.getString("mot_de_passe"));
                utilisateur.setNum_tel(rs.getInt("num_tel"));
                utilisateur.setAdresse(rs.getString("adresse"));
                utilisateur.setSpeciality(rs.getString("speciality"));
                utilisateur.setCategorie_soin(rs.getString("categorie_soin"));
                utilisateur.setImage(rs.getString("image"));
                utilisateur.setImageprofile(rs.getString("imageprofile"));
                utilisateur.setStatut(rs.getString("statut"));
                utilisateur.setReset_code(rs.getInt("reset_code"));

                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs en attente : " + e.getMessage());
        }

        return utilisateurs;
    }

    public void updateStatut(int userId, String newStatut) throws SQLException {
        String req = "UPDATE utilisateur SET statut = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, newStatut);
            pst.setInt(2, userId);
            pst.executeUpdate();
        }
    }

    public void delete(int userId) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }






}