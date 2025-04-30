package com.healthlink.Services;

import com.healthlink.Entities.Care;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entites.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CareService {
    private final Connection connection;

    public CareService(Connection connection) {
        this.connection = connection;
        try {
            System.out.println("CareService connected to database: " + connection.getCatalog());
        } catch (SQLException e) {
            System.err.println("Failed to get database name: " + e.getMessage());
        }
    }

    public boolean addCare(Care care) {
        String sql = "INSERT INTO care (date, address, description, patient_id) VALUES (?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setDate(1, Date.valueOf(care.getDate()));
                pst.setString(2, care.getAddress());
                pst.setString(3, care.getDescription());
                Utilisateur patient = care.getPatient();
                if (patient != null && patient.getId() != 0) {
                    pst.setInt(4, patient.getId());
                } else {
                    System.err.println("No valid patient provided for care");
                    pst.setNull(4, Types.INTEGER);
                }

                int affectedRows = pst.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        care.setId(generatedKeys.getInt(1));
                        connection.commit();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Error adding care: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
        return false;
    }

    public Care getCareById(int careId) {
        String sql = "SELECT c.id, c.date, c.address, c.description, c.patient_id, " +
                "u.id AS patient_id, u.prenom, u.nom, u.imageprofile, u.role_id " +
                "FROM care c " +
                "LEFT JOIN utilisateur u ON c.patient_id = u.id " +
                "WHERE c.id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, careId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Care care = new Care();
                care.setId(rs.getInt("id"));
                care.setDate(rs.getDate("date").toLocalDate());
                care.setAddress(rs.getString("address"));
                care.setDescription(rs.getString("description"));

                int patientId = rs.getInt("patient_id");
                if (!rs.wasNull() && patientId != 0) {
                    Utilisateur patient = new Utilisateur();
                    patient.setId(patientId);
                    patient.setPrenom(rs.getString("prenom"));
                    patient.setNom(rs.getString("nom"));
                    patient.setImageprofile(rs.getString("imageprofile"));
                    Role role = new Role();
                    role.setId(rs.getInt("role_id"));
                    patient.setRole(role);
                    care.setPatient(patient);
                }
                System.out.println("Fetched care ID: " + careId + ", Patient: " +
                        (care.getPatient() != null ? "ID=" + care.getPatient().getId() + ", Name=" +
                                care.getPatient().getPrenom() + " " + care.getPatient().getNom() : "null"));
                return care;
            } else {
                System.out.println("No care found for ID: " + careId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching care: " + e.getMessage());
        }
        return null;
    }

    public List<Care> getAllCares() {
        List<Care> cares = new ArrayList<>();
        String sql = "SELECT c.id, c.date, c.address, c.description, c.patient_id, " +
                "u.id AS patient_id, u.prenom, u.nom, u.imageprofile, u.role_id " +
                "FROM care c " +
                "LEFT JOIN utilisateur u ON c.patient_id = u.id";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Care care = new Care();
                care.setId(rs.getInt("id"));
                care.setDate(rs.getDate("date").toLocalDate());
                care.setAddress(rs.getString("address"));
                care.setDescription(rs.getString("description"));

                int patientId = rs.getInt("patient_id");
                if (!rs.wasNull() && patientId != 0) {
                    Utilisateur patient = new Utilisateur();
                    patient.setId(patientId);
                    patient.setPrenom(rs.getString("prenom"));
                    patient.setNom(rs.getString("nom"));
                    patient.setImageprofile(rs.getString("imageprofile"));
                    Role role = new Role();
                    role.setId(rs.getInt("role_id"));
                    patient.setRole(role);
                    care.setPatient(patient);
                    System.out.println("Fetched care ID: " + care.getId() +
                            ", Patient: ID=" + patient.getId() +
                            ", Name=" + (patient.getPrenom() != null ? patient.getPrenom() : "null") + " " +
                            (patient.getNom() != null ? patient.getNom() : "null"));
                } else {
                    System.out.println("Fetched care ID: " + care.getId() +
                            ", Patient: null (patient_id=" + (rs.wasNull() ? "null" : patientId) + ")");
                }

                cares.add(care);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching cares: " + e.getMessage());
        }
        return cares;
    }

    public boolean updateCare(Care care) {
        String sql = "UPDATE care SET date = ?, address = ?, description = ?, patient_id = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(care.getDate()));
            pst.setString(2, care.getAddress());
            pst.setString(3, care.getDescription());
            Utilisateur patient = care.getPatient();
            if (patient != null && patient.getId() != 0) {
                pst.setInt(4, patient.getId());
            } else {
                pst.setNull(4, Types.INTEGER);
            }
            pst.setInt(5, care.getId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating care: " + e.getMessage());
        }
        return false;
    }

    public void deleteCareResponsesByCareId(int careId) throws SQLException {
        // First, delete all notifications associated with the CareResponse records
        String deleteNotificationsSql = "DELETE n FROM notification n " +
                "INNER JOIN care_response cr ON n.care_response_id = cr.id " +
                "WHERE cr.care_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteNotificationsSql)) {
            stmt.setInt(1, careId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " notifications for care_id=" + careId);
        }

        // Then, delete the CareResponse records
        String deleteCareResponsesSql = "DELETE FROM care_response WHERE care_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteCareResponsesSql)) {
            stmt.setInt(1, careId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " care responses for care_id=" + careId);
        }
    }

    // Overloaded method for console usage (no permission checks)
    public boolean deleteCare(int id) {
        boolean transactionStarted = false;
        try {
            // Start transaction
            connection.setAutoCommit(false);
            transactionStarted = true;

            // Check if the record exists
            String checkSql = "SELECT patient_id FROM care WHERE id = ?";
            Integer patientId = null;
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    patientId = rs.getInt("patient_id");
                    if (rs.wasNull()) patientId = null;
                    System.out.println("Before deletion, found care record with id=" + id + ", patient_id=" + (patientId != null ? patientId : "null"));
                } else {
                    System.out.println("No care record found with id=" + id);
                    connection.rollback();
                    return false;
                }
            }

            // Delete dependent CareResponse records (and their notifications) first
            try {
                deleteCareResponsesByCareId(id);
            } catch (SQLException e) {
                System.err.println("Error deleting care responses for care_id=" + id + ": " + e.getMessage());
                connection.rollback();
                throw new RuntimeException("Error deleting care responses: " + e.getMessage(), e);
            }

            // Now delete the Care record
            String sql = "DELETE FROM care WHERE id = ?";
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setInt(1, id);
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("Successfully deleted care with id=" + id);
                    return true;
                } else {
                    connection.rollback();
                    System.out.println("No care record found with id=" + id + " during deletion");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting care with id=" + id + ": " + e.getMessage());
            if (transactionStarted) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (transactionStarted) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    public boolean deleteCare(int id, Utilisateur currentUser) {
        boolean transactionStarted = false;
        try {
            // Start transaction
            connection.setAutoCommit(false);
            transactionStarted = true;

            // Check if the record exists and the user has permission to delete it
            String checkSql = "SELECT patient_id FROM care WHERE id = ?";
            Integer patientId = null;
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    patientId = rs.getInt("patient_id");
                    if (rs.wasNull()) patientId = null;
                    System.out.println("Before deletion, found care record with id=" + id + ", patient_id=" + (patientId != null ? patientId : "null"));
                } else {
                    System.out.println("No care record found with id=" + id);
                    connection.rollback();
                    return false;
                }
            }

            // Permission check
            if (currentUser == null) {
                System.out.println("No user logged in, cannot delete care with id=" + id);
                connection.rollback();
                return false;
            }
            if (currentUser.getRole().getId() == 3) { // Patient
                if (patientId == null || patientId != currentUser.getId()) {
                    System.out.println("User with id=" + currentUser.getId() + " does not have permission to delete care with id=" + id);
                    connection.rollback();
                    return false; // Patient can only delete their own care records
                }
            }

            // Delete dependent CareResponse records (and their notifications) first
            try {
                deleteCareResponsesByCareId(id);
            } catch (SQLException e) {
                System.err.println("Error deleting care responses for care_id=" + id + ": " + e.getMessage());
                connection.rollback();
                throw new RuntimeException("Error deleting care responses: " + e.getMessage(), e);
            }

            // Now delete the Care record
            String sql = "DELETE FROM care WHERE id = ?";
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setInt(1, id);
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("Successfully deleted care with id=" + id);
                    return true;
                } else {
                    connection.rollback();
                    System.out.println("No care record found with id=" + id + " during deletion");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting care with id=" + id + ": " + e.getMessage());
            if (transactionStarted) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (transactionStarted) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    public boolean careExists(int id) {
        String sql = "SELECT COUNT(*) FROM care WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking care existence: " + e.getMessage());
            return false;
        }
    }
}