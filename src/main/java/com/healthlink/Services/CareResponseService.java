package com.healthlink.Services;

import com.healthlink.Entities.CareResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CareResponseService {
    private final Connection connection;

    public CareResponseService(Connection connection) {
        this.connection = connection;
    }

    public boolean addCareResponse(CareResponse response, int careId) {
        String sql = "INSERT INTO care_response (date_rep, contenu_rep, care_id) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setDate(1, Date.valueOf(response.getDateRep()));
                pst.setString(2, response.getContenuRep());
                pst.setInt(3, careId);

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
        String sql = "SELECT id, date_rep, contenu_rep FROM care_response WHERE care_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, careId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                CareResponse response = new CareResponse();
                response.setId(rs.getInt("id"));
                response.setDateRep(rs.getDate("date_rep").toLocalDate());
                response.setContenuRep(rs.getString("contenu_rep"));
                responses.add(response);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching CareResponses: " + e.getMessage());
        }
        return responses;
    }

    public boolean updateCareResponse(CareResponse careResponse) {
        String sql = "UPDATE care_response SET date_rep = ?, contenu_rep = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(careResponse.getDateRep()));
            pst.setString(2, careResponse.getContenuRep());
            pst.setInt(3, careResponse.getId());

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