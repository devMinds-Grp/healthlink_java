package com.healthlink.Services;

import com.healthlink.Entites.ReponseDon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReponseDonService {
    private Connection connection;

    public ReponseDonService(Connection connection) {
        this.connection = connection;
    }

    public boolean addReponseDon(ReponseDon reponse) {
        String query = "INSERT INTO donation_response (description, blood_donation_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, reponse.getDescription());
            stmt.setInt(2, reponse.getBloodDonationId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding response: " + e.getMessage());
            return false;
        }
    }

    public boolean updateReponseDon(ReponseDon reponse) {
        String query = "UPDATE donation_response SET description = ?, blood_donation_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, reponse.getDescription());
            stmt.setInt(2, reponse.getBloodDonationId());
            stmt.setInt(3, reponse.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating response: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReponseDon(int id) {
        String query = "DELETE FROM donation_response WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting response: " + e.getMessage());
            return false;
        }
    }

    public List<ReponseDon> getAllReponseDon() {
        List<ReponseDon> reponses = new ArrayList<>();
        String query = "SELECT * FROM donation_response";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ReponseDon reponse = new ReponseDon(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getInt("blood_donation_id")
                );
                reponses.add(reponse);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving responses: " + e.getMessage());
        }
        return reponses;
    }

    public ReponseDon getReponseDonById(int id) {
        String query = "SELECT * FROM donation_response WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ReponseDon(
                            rs.getInt("id"),
                            rs.getString("description"),
                            rs.getInt("blood_donation_id")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving response with ID " + id + ": " + e.getMessage());
        }
        return null;
    }
}