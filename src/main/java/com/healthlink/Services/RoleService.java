package com.healthlink.Services;

import com.healthlink.Entites.Role;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Interfaces.InterfaceCRUD;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleService implements InterfaceCRUD<Role> {
    private Connection connection;

    public RoleService() {
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

     
    public void add(Role role) {
        String req = "INSERT INTO role (nom) VALUES (?)";
        try (PreparedStatement pst =  connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, role.getNom());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        role.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du rôle : " + e.getMessage());
        }
    }
     
    public void addPatient(Role role) {
        String req = "INSERT INTO role (nom) VALUES (?)";
        try (PreparedStatement pst =  connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, role.getNom());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        role.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du rôle : " + e.getMessage());
        }
    }
     
    public void addSoignant(Role role) {
        String req = "INSERT INTO role (nom) VALUES (?)";
        try (PreparedStatement pst =  connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, role.getNom());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        role.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du rôle : " + e.getMessage());
        }
    }
     
    public void addMedecin(Role role) {
        String req = "INSERT INTO role (nom) VALUES (?)";
        try (PreparedStatement pst =  connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, role.getNom());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        role.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du rôle : " + e.getMessage());
        }
    }

     
    public void update(Role role) {
        String req = "UPDATE role SET nom = ? WHERE id = ?";
        try (PreparedStatement pst =  connection.prepareStatement(req)) {
            pst.setString(1, role.getNom());
            pst.setInt(2, role.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du rôle : " + e.getMessage());
        }
    }

     
    public void delete(Role role) {
        String req = "DELETE FROM role WHERE id = ?";
        try (PreparedStatement pst =  connection.prepareStatement(req)) {
            pst.setInt(1, role.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du rôle : " + e.getMessage());
        }
    }

     
    public List<Role> find() {
        String req = "SELECT * FROM role";
        List<Role> roles = new ArrayList<>();

        try (Statement st =  connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Role r = new Role();
                r.setId(rs.getInt("id"));
                r.setNom(rs.getString("nom"));
                roles.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des rôles : " + e.getMessage());
        }
        return roles;
    }


     
    public List<Utilisateur> findAllPatients() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 3";
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
     
    public List<Utilisateur> findAllMedecins() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 2";
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
     
    public List<Utilisateur> findAllSoignants() {
        String req = "SELECT u.*, r.id as role_id, r.nom as role_nom FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.role_id = 4";
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
}