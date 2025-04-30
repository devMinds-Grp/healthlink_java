package com.healthlink.Services;

import com.healthlink.Entites.DonDuSang;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List; // Added import for List
import java.util.regex.Pattern;

public class DonDuSangService {
    private final Connection connection;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$"); // Basic phone number validation

    public DonDuSangService(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Database connection cannot be null");
        }
        this.connection = connection;
    }

    // Validate phone number format
    private boolean isValidPhoneNumber(String numTel) {
        if (numTel == null) return false;
        return PHONE_PATTERN.matcher(numTel).matches();
    }

    // Check if a donation with the same description, lieu, and date already exists
    public boolean donationExists(String description, String lieu, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM blood_donation WHERE description = ? AND lieu = ? AND date = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, description);
            stmt.setString(2, lieu);
            stmt.setDate(3, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking for existing donation: " + e.getMessage());
        }
        return false;
    }

    // Add a new donation
    public boolean addDonDuSang(DonDuSang don) {
        if (don == null) {
            System.err.println("Error: Cannot add a null DonDuSang object");
            return false;
        }
        if (don.getDescription() == null || don.getLieu() == null || don.getDate() == null || don.getNumTel() == null) {
            System.err.println("Error: All fields (description, lieu, date, numTel) must be non-null");
            return false;
        }
        if (!isValidPhoneNumber(don.getNumTel())) {
            System.err.println("Error: Invalid phone number format: " + don.getNumTel());
            return false;
        }
        if (donationExists(don.getDescription(), don.getLieu(), don.getDate())) {
            System.err.println("Error: A donation with the same description, lieu, and date already exists");
            return false;
        }

        String sql = "INSERT INTO blood_donation (description, lieu, date, num_tel) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, don.getDescription());
            stmt.setString(2, don.getLieu());
            stmt.setDate(3, java.sql.Date.valueOf(don.getDate()));
            stmt.setString(4, don.getNumTel());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Donation added successfully: " + don);
                return true;
            } else {
                System.err.println("Failed to add donation: No rows affected");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error adding donation: " + e.getMessage());
            return false;
        }
    }

    // Get all donations
    public List<DonDuSang> getAllDonDuSang() {
        List<DonDuSang> donations = new ArrayList<>();
        String sql = "SELECT * FROM blood_donation";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String description = rs.getString("description");
                String lieu = rs.getString("lieu");
                java.sql.Date date = rs.getDate("date");
                String numTel = rs.getString("num_tel");

                if (description == null || lieu == null || date == null || numTel == null) {
                    System.err.println("Skipping invalid donation record with ID " + rs.getInt("id") + ": Missing required fields");
                    continue;
                }

                DonDuSang don = new DonDuSang();
                don.setId(rs.getInt("id"));
                don.setDescription(description);
                don.setLieu(lieu);
                don.setDate(date.toLocalDate());
                don.setNumTel(numTel);
                donations.add(don);
            }
            System.out.println("Retrieved " + donations.size() + " donations successfully");
        } catch (SQLException e) {
            System.err.println("Error retrieving donations: " + e.getMessage());
        }
        return donations;
    }

    // Get a donation by ID
    public DonDuSang getDonDuSangById(int id) {
        if (id <= 0) {
            System.err.println("Error: Invalid donation ID: " + id);
            return null;
        }

        String sql = "SELECT * FROM blood_donation WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String description = rs.getString("description");
                    String lieu = rs.getString("lieu");
                    java.sql.Date date = rs.getDate("date");
                    String numTel = rs.getString("num_tel");

                    if (description == null || lieu == null || date == null || numTel == null) {
                        System.err.println("Invalid donation record with ID " + id + ": Missing required fields");
                        return null;
                    }

                    DonDuSang don = new DonDuSang();
                    don.setId(rs.getInt("id"));
                    don.setDescription(description);
                    don.setLieu(lieu);
                    don.setDate(date.toLocalDate());
                    don.setNumTel(numTel);
                    return don;
                } else {
                    System.err.println("No donation found with ID: " + id);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving donation with ID " + id + ": " + e.getMessage());
            return null;
        }
    }

    // Update an existing donation
    public boolean updateDonDuSang(DonDuSang don) {
        if (don == null) {
            System.err.println("Error: Cannot update a null DonDuSang object");
            return false;
        }
        if (don.getId() <= 0) {
            System.err.println("Error: Invalid donation ID: " + don.getId());
            return false;
        }
        if (don.getDescription() == null || don.getLieu() == null || don.getDate() == null || don.getNumTel() == null) {
            System.err.println("Error: All fields (description, lieu, date, numTel) must be non-null");
            return false;
        }
        if (!isValidPhoneNumber(don.getNumTel())) {
            System.err.println("Error: Invalid phone number format: " + don.getNumTel());
            return false;
        }

        String sql = "UPDATE blood_donation SET description = ?, lieu = ?, date = ?, num_tel = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, don.getDescription());
            stmt.setString(2, don.getLieu());
            stmt.setDate(3, java.sql.Date.valueOf(don.getDate()));
            stmt.setString(4, don.getNumTel());
            stmt.setInt(5, don.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Donation updated successfully: " + don);
                return true;
            } else {
                System.err.println("Failed to update donation with ID " + don.getId() + ": No rows affected");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating donation: " + e.getMessage());
            return false;
        }
    }

    // Delete a donation
    public boolean deleteDonDuSang(int id) {
        if (id <= 0) {
            System.err.println("Error: Invalid donation ID: " + id);
            return false;
        }

        String sql = "DELETE FROM blood_donation WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Donation deleted successfully: ID " + id);
                return true;
            } else {
                System.err.println("Failed to delete donation with ID " + id + ": No rows affected");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting donation with ID " + id + ": " + e.getMessage());
            return false;
        }
    }
}