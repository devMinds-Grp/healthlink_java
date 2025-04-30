package com.healthlink.Services;

import com.healthlink.Entities.CareResponse;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entites.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CareResponseService {
    private final Connection connection;

    public CareResponseService(Connection connection) {
        this.connection = connection;
    }

    public int addCareResponseAndGetId(CareResponse response, int careId) {
        String sql = "INSERT INTO care_response (care_id, user_id, patient_id, soignant_id, date_rep, contenu_rep) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Set care_id
                pst.setInt(1, careId);

                // Set user_id (soignant)
                Utilisateur user = response.getUser();
                if (user == null || user.getId() == 0) {
                    System.err.println("Cannot add CareResponse: user_id is null or invalid");
                    connection.rollback();
                    return -1;
                }
                pst.setInt(2, user.getId());

                // Set patient_id
                Utilisateur patient = response.getPatient();
                if (patient != null && patient.getId() != 0) {
                    pst.setInt(3, patient.getId());
                } else {
                    System.err.println("Cannot add CareResponse: patient_id is null or invalid for care_id=" + careId);
                    // Optionally fetch from care table as a fallback
                    String careSql = "SELECT patient_id FROM care WHERE id = ?";
                    try (PreparedStatement carePst = connection.prepareStatement(careSql)) {
                        carePst.setInt(1, careId);
                        ResultSet rs = carePst.executeQuery();
                        if (rs.next()) {
                            int patientId = rs.getInt("patient_id");
                            if (!rs.wasNull()) {
                                pst.setInt(3, patientId);
                            } else {
                                System.err.println("No patient_id found in care table for care_id=" + careId);
                                connection.rollback();
                                return -1;
                            }
                        } else {
                            System.err.println("Care record not found for care_id=" + careId);
                            connection.rollback();
                            return -1;
                        }
                    }
                }

                // Set soignant_id
                Utilisateur soignant = response.getSoignant();
                if (soignant != null && soignant.getId() != 0) {
                    pst.setInt(4, soignant.getId());
                } else {
                    System.err.println("Cannot add CareResponse: soignant_id is null or invalid");
                    connection.rollback();
                    return -1;
                }

                // Set date_rep and contenu_rep
                pst.setDate(5, Date.valueOf(response.getDateRep()));
                pst.setString(6, response.getContenuRep());

                // Log the values being inserted
                System.out.println("Inserting CareResponse: care_id=" + careId +
                        ", user_id=" + user.getId() +
                        ", patient_id=" + (patient != null ? patient.getId() : "null") +
                        ", soignant_id=" + (soignant != null ? soignant.getId() : "null") +
                        ", date_rep=" + response.getDateRep() +
                        ", contenu_rep=" + response.getContenuRep());

                int affectedRows = pst.executeUpdate();
                if (affectedRows == 0) {
                    System.err.println("No rows affected when inserting CareResponse for care_id=" + careId);
                    connection.rollback();
                    return -1;
                }

                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int careResponseId = generatedKeys.getInt(1);
                        connection.commit();
                        System.out.println("Successfully added CareResponse with id=" + careResponseId);
                        return careResponseId;
                    }
                }
                connection.rollback();
                return -1;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Error adding CareResponse for care_id=" + careId + ": " + e.getMessage());
            return -1;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public boolean addCareResponse(CareResponse response, int careId) {
        String sql = "INSERT INTO care_response (date_rep, contenu_rep, care_id, user_id, patient_id, soignant_id) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setDate(1, Date.valueOf(response.getDateRep()));
                pst.setString(2, response.getContenuRep());
                pst.setInt(3, careId);

                // Set user_id (soignant)
                Utilisateur user = response.getUser();
                if (user != null && user.getId() != 0) {
                    pst.setInt(4, user.getId());
                } else {
                    System.err.println("No valid user provided for care response");
                    pst.setNull(4, Types.INTEGER);
                }

                // Set patient_id (from care)
                Utilisateur patient = response.getPatient();
                if (patient != null && patient.getId() != 0) {
                    pst.setInt(5, patient.getId());
                } else {
                    // Fetch patient_id from care
                    String careSql = "SELECT patient_id FROM care WHERE id = ?";
                    try (PreparedStatement carePst = connection.prepareStatement(careSql)) {
                        carePst.setInt(1, careId);
                        ResultSet rs = carePst.executeQuery();
                        if (rs.next()) {
                            int patientId = rs.getInt("patient_id");
                            if (!rs.wasNull()) {
                                pst.setInt(5, patientId);
                            } else {
                                pst.setNull(5, Types.INTEGER);
                            }
                        } else {
                            pst.setNull(5, Types.INTEGER);
                        }
                    }
                }

                // Set soignant_id
                Utilisateur soignant = response.getSoignant();
                if (soignant != null && soignant.getId() != 0) {
                    pst.setInt(6, soignant.getId());
                } else {
                    System.err.println("No valid soignant provided for care response");
                    pst.setNull(6, Types.INTEGER);
                }

                int affectedRows = pst.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        response.setId(generatedKeys.getInt(1));
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
            System.err.println("Error adding care response: " + e.getMessage());
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

    public List<CareResponse> getResponsesByCareId(int careId) {
        List<CareResponse> responses = new ArrayList<>();
        String sql = "SELECT cr.id, cr.date_rep, cr.contenu_rep, cr.care_id, cr.user_id, cr.patient_id, cr.soignant_id, " +
                "u.id AS user_id, u.prenom AS user_prenom, u.nom AS user_nom, u.imageprofile AS user_image, u.role_id AS user_role_id, " +
                "p.id AS patient_id, p.prenom AS patient_prenom, p.nom AS patient_nom, p.imageprofile AS patient_image, p.role_id AS patient_role_id, " +
                "s.id AS soignant_id, s.prenom AS soignant_prenom, s.nom AS soignant_nom, s.imageprofile AS soignant_image, s.role_id AS soignant_role_id " +
                "FROM care_response cr " +
                "LEFT JOIN utilisateur u ON cr.user_id = u.id " +
                "LEFT JOIN utilisateur p ON cr.patient_id = p.id " +
                "LEFT JOIN utilisateur s ON cr.soignant_id = s.id " +
                "WHERE cr.care_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, careId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                CareResponse response = new CareResponse();
                response.setId(rs.getInt("id"));
                response.setDateRep(rs.getDate("date_rep").toLocalDate());
                response.setContenuRep(rs.getString("contenu_rep"));

                // Set user
                int userId = rs.getInt("user_id");
                if (!rs.wasNull() && userId != 0) {
                    Utilisateur user = new Utilisateur();
                    user.setId(userId);
                    user.setPrenom(rs.getString("user_prenom"));
                    user.setNom(rs.getString("user_nom"));
                    user.setImageprofile(rs.getString("user_image"));
                    Role role = new Role();
                    role.setId(rs.getInt("user_role_id"));
                    user.setRole(role);
                    response.setUser(user);
                }

                // Set patient
                int patientId = rs.getInt("patient_id");
                if (!rs.wasNull() && patientId != 0) {
                    Utilisateur patient = new Utilisateur();
                    patient.setId(patientId);
                    patient.setPrenom(rs.getString("patient_prenom"));
                    patient.setNom(rs.getString("patient_nom"));
                    patient.setImageprofile(rs.getString("patient_image"));
                    Role role = new Role();
                    role.setId(rs.getInt("patient_role_id"));
                    patient.setRole(role);
                    response.setPatient(patient);
                }

                // Set soignant
                int soignantId = rs.getInt("soignant_id");
                if (!rs.wasNull() && soignantId != 0) {
                    Utilisateur soignant = new Utilisateur();
                    soignant.setId(soignantId);
                    soignant.setPrenom(rs.getString("soignant_prenom"));
                    soignant.setNom(rs.getString("soignant_nom"));
                    soignant.setImageprofile(rs.getString("soignant_image"));
                    Role role = new Role();
                    role.setId(rs.getInt("soignant_role_id"));
                    soignant.setRole(role);
                    response.setSoignant(soignant);
                }

                System.out.println("Fetched response ID: " + response.getId() +
                        ", User: " + (response.getUser() != null ? "ID=" + response.getUser().getId() + ", Name=" + response.getUser().getPrenom() + " " + response.getUser().getNom() : "null") +
                        ", Patient: " + (response.getPatient() != null ? "ID=" + response.getPatient().getId() + ", Name=" + response.getPatient().getPrenom() + " " + response.getPatient().getNom() : "null") +
                        ", Soignant: " + (response.getSoignant() != null ? "ID=" + response.getSoignant().getId() + ", Name=" + response.getSoignant().getPrenom() + " " + response.getSoignant().getNom() : "null"));
                responses.add(response);
            }
            if (responses.isEmpty()) {
                System.out.println("No responses found for care_id=" + careId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching CareResponses for care_id=" + careId + ": " + e.getMessage());
        }
        return responses;
    }

    public boolean updateCareResponse(CareResponse careResponse) {
        String sql = "UPDATE care_response SET date_rep = ?, contenu_rep = ?, user_id = ?, patient_id = ?, soignant_id = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(careResponse.getDateRep()));
            pst.setString(2, careResponse.getContenuRep());

            Utilisateur user = careResponse.getUser();
            if (user != null && user.getId() != 0) {
                pst.setInt(3, user.getId());
            } else {
                pst.setNull(3, Types.INTEGER);
            }

            Utilisateur patient = careResponse.getPatient();
            if (patient != null && patient.getId() != 0) {
                pst.setInt(4, patient.getId());
            } else {
                pst.setNull(4, Types.INTEGER);
            }

            Utilisateur soignant = careResponse.getSoignant();
            if (soignant != null && soignant.getId() != 0) {
                pst.setInt(5, soignant.getId());
            } else {
                pst.setNull(5, Types.INTEGER);
            }

            pst.setInt(6, careResponse.getId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating CareResponse: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteCareResponse(int id) {
        String sql = "DELETE FROM care_response WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting CareResponse: " + e.getMessage());
        }
        return false;
    }

    public boolean responseExists(int id) {
        String sql = "SELECT COUNT(*) FROM care_response WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking CareResponse existence: " + e.getMessage());
        }
        return false;
    }
}