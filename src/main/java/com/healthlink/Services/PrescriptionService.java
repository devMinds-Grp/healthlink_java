package com.healthlink.Services;

import com.healthlink.Entities.Prescription;
import com.healthlink.utils.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionService {

    public PrescriptionService() {
        try {
            MyDB.getInstance().getConnection();
            System.out.println("PrescriptionService: Database connection established");
        } catch (SQLException e) {
            System.err.println("PrescriptionService: Failed to establish database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return MyDB.getInstance().getConnection();
    }

    public List<Prescription> getAllPrescriptions() {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "CONCAT(u.prenom, ' ', u.nom) AS patient_name, " +
                "a.date AS appointment_date " +
                "FROM prescription p " +
                "LEFT JOIN appointment a ON p.RDVCard_id = a.id " +
                "LEFT JOIN utilisateur u ON a.patient_id = u.id AND u.role_id = 3";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            System.out.println("Exécution de la requête pour récupérer les prescriptions");
            while (rs.next()) {
                Prescription prescription = new Prescription();
                prescription.setId(rs.getInt("id"));
                prescription.setNomMedicament(rs.getString("nom_medicament"));
                prescription.setDosage(rs.getString("dosage"));
                prescription.setDuree(rs.getString("duree"));
                prescription.setNotes(rs.getString("notes"));
                prescription.setRdvCardId(rs.getInt("RDVCard_id"));
                prescription.setPatientName(rs.getString("patient_name") != null ? rs.getString("patient_name") : "Utilisateur inconnu");
                prescription.setAppointmentDate(rs.getString("appointment_date"));
                prescriptions.add(prescription);
                System.out.println("Prescription chargée : " + prescription);
            }
            System.out.println("Total des prescriptions chargées : " + prescriptions.size());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des prescriptions : " + e.getMessage());
            e.printStackTrace();
        }
        return prescriptions;
    }

    public boolean addPrescription(Prescription prescription) {
        String sql = "INSERT INTO prescription (nom_medicament, dosage, duree, notes, RDVCard_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, prescription.getNomMedicament());
            pst.setString(2, prescription.getDosage());
            pst.setString(3, prescription.getDuree());
            pst.setString(4, prescription.getNotes());
            pst.setInt(5, prescription.getRdvCardId());

            int affectedRows = pst.executeUpdate();
            System.out.println("Prescription ajoutée, lignes affectées : " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajout prescription : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePrescription(Prescription prescription) {
        String sql = "UPDATE prescription SET nom_medicament=?, dosage=?, duree=?, notes=?, RDVCard_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, prescription.getNomMedicament());
            pst.setString(2, prescription.getDosage());
            pst.setString(3, prescription.getDuree());
            pst.setString(4, prescription.getNotes());
            pst.setInt(5, prescription.getRdvCardId());
            pst.setInt(6, prescription.getId());

            int affectedRows = pst.executeUpdate();
            System.out.println("Prescription mise à jour, lignes affectées : " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour prescription : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePrescription(int id) {
        String sql = "DELETE FROM prescription WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            int affectedRows = pst.executeUpdate();
            System.out.println("Prescription supprimée, lignes affectées : " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression prescription : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}