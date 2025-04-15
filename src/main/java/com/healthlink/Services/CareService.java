package com.healthlink.Services;

import com.healthlink.Entities.Care;
import com.healthlink.Entities.CareResponse;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CareService {
    private Connection connection;
    private CareResponseService responseService;

    public CareService(Connection connection) {
        this.connection = connection;
        this.responseService = new CareResponseService(connection);
    }

    // CREATE
    public boolean addCare(Care care) {
        String sql = "INSERT INTO care (date, address, description) VALUES (?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters
            pst.setDate(1, Date.valueOf(care.getDate()));
            pst.setString(2, care.getAddress());
            pst.setString(3, care.getDescription());

            System.out.println("Executing SQL: " + pst.toString());

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("No rows affected - insert failed");
                return false;
            }

            // Get generated ID
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    care.setId(generatedKeys.getInt(1));
                    System.out.println("Insert successful, generated ID: " + care.getId());
                    return true;
                } else {
                    System.err.println("No ID obtained - insert failed");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error adding care: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // READ ALL with responses
    public List<Care> getAllCares() {
        List<Care> cares = new ArrayList<>();
        String sql = "SELECT id, date, address, description FROM care";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Care care = new Care();
                care.setId(rs.getInt("id"));
                care.setDate(rs.getDate("date").toLocalDate());
                care.setAddress(rs.getString("address"));
                care.setDescription(rs.getString("description"));

                // Load associated responses
                List<CareResponse> responses = responseService.getResponsesByCareId(care.getId());
                for (CareResponse response : responses) {
                    care.addResponse(response);
                }

                cares.add(care);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching cares: " + e.getMessage());
            e.printStackTrace();
        }
        return cares;
    }

    // UPDATE
    public boolean updateCare(Care care) {
        String sql = "UPDATE care SET date=?, address=?, description=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(care.getDate()));
            pst.setString(2, care.getAddress());
            pst.setString(3, care.getDescription());
            pst.setInt(4, care.getId());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Care updated successfully, ID: " + care.getId());
                return true;
            } else {
                System.err.println("No rows affected - update failed for ID: " + care.getId());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating care: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // DELETE
    public boolean deleteCare(int id) {
        try {
            connection.setAutoCommit(false); // Start transaction
            try {
                // Delete associated responses
                String deleteResponsesSql = "DELETE FROM care_response WHERE care_id = ?";
                try (PreparedStatement pst = connection.prepareStatement(deleteResponsesSql)) {
                    pst.setInt(1, id);
                    pst.executeUpdate();
                }

                // Delete care
                String sql = "DELETE FROM care WHERE id = ?";
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
                    pst.setInt(1, id);
                    int affectedRows = pst.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Care deleted successfully, ID: " + id);
                        connection.commit();
                        return true;
                    } else {
                        System.err.println("No rows affected - delete failed for ID: " + id);
                        connection.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Error during delete transaction: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error setting auto-commit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean careExists(int careId) {
        String sql = "SELECT 1 FROM care WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, careId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking care existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Map<Integer, LocalDate> getLatestResponseDates() {
        Map<Integer, LocalDate> responseDates = new HashMap<>();
        String sql = "SELECT care_id, MAX(date) as latest_date FROM care_response GROUP BY care_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date sqlDate = rs.getDate("latest_date");
                if (sqlDate != null) {
                    responseDates.put(rs.getInt("care_id"), sqlDate.toLocalDate());
                }
            }
            System.out.println("Fetched latest response dates: " + responseDates.size() + " entries");
        } catch (SQLException e) {
            System.err.println("Error fetching latest response dates: " + e.getMessage());
            e.printStackTrace();
        }
        return responseDates;
    }
}