package com.healthlink.Services;

import com.healthlink.Entities.Prescription;
import com.healthlink.utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionService {
    private Connection connection;

    public PrescriptionService() {
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

    // CREATE
    public boolean addPrescription(Prescription prescription) {
        String query = "INSERT INTO prescription (nom_medicament, dosage, duree, notes, RDVCard_id) VALUES (?, ?, ?, ?, ?)";
        System.out.println("Attempting to add prescription: " + prescription);
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, prescription.getNomMedicament());
            statement.setString(2, prescription.getDosage());
            statement.setString(3, prescription.getDuree());
            statement.setString(4, prescription.getNotes());
            if (prescription.getRdvCardId() != null) {
                statement.setInt(5, prescription.getRdvCardId());
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            System.out.println("Exécution de la requête : " + query);
            System.out.println("Paramètres : nom_medicament=" + prescription.getNomMedicament() +
                    ", dosage=" + prescription.getDosage() +
                    ", duree=" + prescription.getDuree() +
                    ", notes=" + prescription.getNotes() +
                    ", RDVCard_id=" + prescription.getRdvCardId());

            int affectedRows = statement.executeUpdate();
            System.out.println("Rows affected: " + affectedRows);
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        prescription.setId(generatedKeys.getInt(1));
                        System.out.println("Generated ID: " + prescription.getId());
                    }
                }
                return true;
            }
            System.out.println("No rows affected, insertion failed");
            return false;
        } catch (SQLException e) {
            logError("Erreur lors de l'ajout de la prescription", e);
            return false;
        }
    }

    // READ ALL
    public List<Prescription> getAllPrescriptions() {
        List<Prescription> prescriptions = new ArrayList<>();
        String req = "SELECT * FROM prescription";
        System.out.println("Executing query to get all prescriptions");

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getInt("id"));
                p.setNomMedicament(rs.getString("nom_medicament"));
                p.setDosage(rs.getString("dosage"));
                p.setDuree(rs.getString("duree"));
                p.setNotes(rs.getString("notes"));
                int rdvCardId = rs.getInt("RDVCard_id");
                if (!rs.wasNull()) {
                    p.setRdvCardId(rdvCardId);
                } else {
                    p.setRdvCardId(null);
                }
                prescriptions.add(p);
                System.out.println("Loaded prescription: " + p);
            }
            System.out.println("Total prescriptions loaded: " + prescriptions.size());
        } catch (SQLException e) {
            logError("Erreur lors de la lecture des prescriptions", e);
        }
        return prescriptions;
    }

    // UPDATE
    public boolean updatePrescription(Prescription prescription) {
        if (!validatePrescription(prescription)) {
            return false;
        }

        String req = "UPDATE prescription SET nom_medicament=?, dosage=?, duree=?, notes=?, RDVCard_id=? WHERE id=?";
        System.out.println("Attempting to update prescription: " + prescription);

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pst = connection.prepareStatement(req)) {
                pst.setString(1, prescription.getNomMedicament());
                pst.setString(2, prescription.getDosage());
                pst.setString(3, prescription.getDuree());
                pst.setString(4, prescription.getNotes());
                if (prescription.getRdvCardId() != null) {
                    pst.setInt(5, prescription.getRdvCardId());
                } else {
                    pst.setNull(5, Types.INTEGER);
                }
                pst.setInt(6, prescription.getId());

                System.out.println("Exécution de la requête : " + req);
                System.out.println("Paramètres : nom_medicament=" + prescription.getNomMedicament() +
                        ", dosage=" + prescription.getDosage() +
                        ", duree=" + prescription.getDuree() +
                        ", notes=" + prescription.getNotes() +
                        ", RDVCard_id=" + prescription.getRdvCardId() +
                        ", id=" + prescription.getId());

                int affectedRows = pst.executeUpdate();
                System.out.println("Rows affected: " + affectedRows);
                if (affectedRows == 0) {
                    connection.rollback();
                    System.out.println("No rows affected, update failed");
                    return false;
                }

                connection.commit();
                return true;
            }
        } catch (SQLException e) {
            rollbackQuietly();
            logError("Erreur lors de la mise à jour de la prescription", e);
            return false;
        } finally {
            resetAutoCommit();
        }
    }

    // DELETE
    public boolean deletePrescription(int id) {
        String req = "DELETE FROM prescription WHERE id=?";
        System.out.println("Attempting to delete prescription with ID: " + id);

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pst = connection.prepareStatement(req)) {
                pst.setInt(1, id);

                System.out.println("Exécution de la requête : " + req);
                System.out.println("Paramètres : id=" + id);

                int affectedRows = pst.executeUpdate();
                System.out.println("Rows affected: " + affectedRows);
                if (affectedRows == 0) {
                    connection.rollback();
                    System.out.println("No rows affected, deletion failed");
                    return false;
                }

                connection.commit();
                return true;
            }
        } catch (SQLException e) {
            rollbackQuietly();
            logError("Erreur lors de la suppression de la prescription", e);
            return false;
        } finally {
            resetAutoCommit();
        }
    }

    // Utility Methods
    private boolean validatePrescription(Prescription prescription) {
        if (prescription == null) {
            System.err.println("La prescription est null");
            return false;
        }

        if (prescription.getNomMedicament() == null || prescription.getNomMedicament().trim().isEmpty()) {
            System.err.println("Le nom du médicament est requis");
            return false;
        }

        if (prescription.getDosage() == null || prescription.getDosage().trim().isEmpty()) {
            System.err.println("Le dosage est requis");
            return false;
        }

        return true;
    }

    private void rollbackQuietly() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            System.err.println("Échec du rollback: " + e.getMessage());
        }
    }

    private void resetAutoCommit() {
        try {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Échec de la réinitialisation de l'auto-commit: " + e.getMessage());
        }
    }

    private void logError(String message, SQLException e) {
        System.err.println(message);
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
    }
}