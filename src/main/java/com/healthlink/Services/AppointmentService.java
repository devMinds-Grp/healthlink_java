package com.healthlink.Services;

import com.healthlink.Entities.Appointment;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private Connection connection;

    public AppointmentService() {
        try {
            this.connection = MyDB.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur connexion DB: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public class AddResult {
        private boolean success;
        private String errorMessage;

        public AddResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public AddResult addAppointment(Appointment appointment) {
        String req = "INSERT INTO appointment (date, type, statut) VALUES (?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, appointment.getDate());
            pst.setString(2, appointment.getType());
            pst.setString(3, "en attente");

            System.out.println("Exécution de la requête : " + req);
            System.out.println("Paramètres : date=" + appointment.getDate() + ", type=" + appointment.getType() +
                    ", statut=en attente");

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        appointment.setId(keys.getInt(1));
                        System.out.println("Rendez-vous ajouté avec l'ID : " + appointment.getId());
                    }
                }
                return new AddResult(true, null);
            } else {
                System.err.println("Aucune ligne affectée lors de l'insertion.");
                return new AddResult(false, "Aucune ligne affectée lors de l'insertion.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout rendez-vous: " + e.getMessage());
            e.printStackTrace();
            return new AddResult(false, "Erreur SQL : " + e.getMessage());
        }
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String req = "SELECT * FROM appointment";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                try {
                    String status = rs.getString("statut");
                    System.out.println("Database status value: " + status);
                    Appointment a = new Appointment();
                    a.setId(rs.getInt("id"));
                    a.setDate(rs.getString("date"));
                    a.setType(rs.getString("type"));
                    a.setStatus(status);
                    appointments.add(a);
                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping invalid appointment record: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture rendez-vous: " + e.getMessage());
        }
        return appointments;
    }

    public boolean updateAppointment(Appointment appointment) {
        String req = "UPDATE appointment SET date=?, type=?, statut=? WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, appointment.getDate());
            pst.setString(2, appointment.getType());
            pst.setString(3, appointment.getStatus());
            pst.setInt(4, appointment.getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour rendez-vous: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAppointment(int id) {
        String req = "DELETE FROM appointment WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression rendez-vous: " + e.getMessage());
            return false;
        }
    }

    public Appointment getAppointmentById(int id) {
        String req = "SELECT * FROM appointment WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Appointment a = new Appointment();
                    a.setId(rs.getInt("id"));
                    a.setDate(rs.getString("date"));
                    a.setType(rs.getString("type"));
                    a.setStatus(rs.getString("statut"));
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture rendez-vous: " + e.getMessage());
        }
        return null;
    }
}