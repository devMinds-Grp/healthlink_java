package com.healthlink.Services;

import com.healthlink.Entities.Appointment;
import com.healthlink.utils.MyDB;
import com.healthlink.Entites.Utilisateur;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppointmentService {
    private static final SMSService smsService = new SMSService();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static class AddResult {
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

    public AppointmentService() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Application is shutting down at " + ZonedDateTime.now(ZoneId.of("Africa/Tunis")));
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    System.out.println("Scheduler did not terminate gracefully.");
                } else {
                    System.out.println("Scheduler terminated gracefully.");
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                System.err.println("Scheduler shutdown interrupted: " + e.getMessage());
            }
        }));
    }

    private Connection getConnection() throws SQLException {
        return MyDB.getInstance().getConnection();
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty() && phoneNumber.matches("\\d{8}")) {
            return "+216" + phoneNumber;
        }
        return null;
    }

    public void scheduleReminder(Appointment appointment) {
        if (appointment.getPatientPhone() == null || appointment.getStatus() == null || !appointment.getStatus().equals("confirmé")) {
            System.err.println("Cannot schedule reminder: Invalid phone number or appointment not confirmed for ID " + appointment.getId());
            return;
        }

        try {
            // Parse appointment date (assuming format "yyyy-MM-dd")
            LocalDate appointmentDate = LocalDate.parse(appointment.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // Calculate reminder date (1 day before appointment)
            LocalDate reminderDate = appointmentDate.minusDays(1);
            // Set reminder time to 18:36 Africa/Tunis
            ZoneId tunisZone = ZoneId.of("Africa/Tunis");
            ZonedDateTime reminderTime = ZonedDateTime.of(reminderDate, LocalTime.of(19, 50), tunisZone);
            ZonedDateTime now = ZonedDateTime.now(tunisZone);

            // Log current time and reminder time for debugging
            System.out.println("Current time: " + now);
            System.out.println("Reminder time: " + reminderTime);

            // If reminder time is in the past, log and skip
            if (reminderTime.isBefore(now)) {
                System.err.println("Reminder time is in the past for appointment ID " + appointment.getId() + ". Skipping scheduling.");
                return;
            }

            // Calculate delay until reminder time
            long delaySeconds = Duration.between(now, reminderTime).getSeconds();
            System.out.println("Delay until reminder: " + delaySeconds + " seconds");

            // If delay is very short (less than 60 seconds), send SMS immediately
            if (delaySeconds < 60) {
                System.out.println("Delay is less than 60 seconds for appointment ID " + appointment.getId() + ". Sending SMS immediately.");
                try {
                    smsService.sendReminderMessage(
                            appointment.getPatientPhone(),
                            appointment.getDoctorName(),
                            appointment.getDate()
                    );
                    System.out.println("Rappel SMS envoyé immédiatement pour le rendez-vous ID: " + appointment.getId());
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'envoi immédiat du rappel SMS pour le rendez-vous ID " + appointment.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
                return;
            }

            // Schedule one-time reminder task
            scheduler.schedule(() -> {
                try {
                    System.out.println("Executing scheduled reminder task for appointment ID " + appointment.getId() + " at " + ZonedDateTime.now(tunisZone));
                    smsService.sendReminderMessage(
                            appointment.getPatientPhone(),
                            appointment.getDoctorName(),
                            appointment.getDate()
                    );
                    System.out.println("Rappel SMS envoyé pour le rendez-vous ID: " + appointment.getId());
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'envoi du rappel SMS pour le rendez-vous ID " + appointment.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }, delaySeconds, TimeUnit.SECONDS);

            System.out.println("Reminder scheduled for appointment ID " + appointment.getId() + " at " + reminderTime + " (delay: " + delaySeconds + " seconds)");
        } catch (Exception e) {
            System.err.println("Erreur lors de la planification du rappel pour le rendez-vous ID " + appointment.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public AddResult addAppointment(Appointment appointment) {
        if (appointment == null) {
            return new AddResult(false, "Rendez-vous ne peut pas être null.");
        }
        String sql = "INSERT INTO appointment (date, type, statut, patient_id, doctor_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Utilisateur connectedUser = AuthService.getConnectedUtilisateur();
            if (connectedUser == null || connectedUser.getRole().getId() != 3) {
                return new AddResult(false, "Utilisateur non connecté ou non autorisé (patient requis).");
            }

            pst.setString(1, appointment.getDate());
            pst.setString(2, appointment.getType());
            pst.setString(3, appointment.getStatus().getValue());
            pst.setInt(4, connectedUser.getId());
            pst.setInt(5, appointment.getDoctorId());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        appointment.setId(keys.getInt(1));
                        appointment.setPatientId(connectedUser.getId());
                    }
                }
                return new AddResult(true, null);
            } else {
                return new AddResult(false, "Aucune ligne affectée lors de l'insertion.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout rendez-vous: " + e.getMessage());
            e.printStackTrace();
            return new AddResult(false, "Erreur SQL : " + e.getMessage());
        }
    }

    public boolean updateAppointment(Appointment appointment) {
        String sql = "UPDATE appointment SET date=?, type=?, statut=?, patient_id=?, doctor_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            Utilisateur connectedUser = AuthService.getConnectedUtilisateur();
            if (connectedUser == null) {
                System.err.println("Utilisateur non connecté. Impossible de mettre à jour le rendez-vous.");
                return false;
            }

            pst.setString(1, appointment.getDate());
            pst.setString(2, appointment.getType());
            pst.setString(3, appointment.getStatus().getValue());
            pst.setInt(4, appointment.getPatientId());
            pst.setInt(5, appointment.getDoctorId());
            pst.setInt(6, appointment.getId());

            int affectedRows = pst.executeUpdate();
            // If the appointment is confirmed, schedule a reminder
            if (affectedRows > 0 && appointment.getStatus().equals("confirmé")) {
                scheduleReminder(appointment);
            }
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour rendez-vous: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, " +
                "CONCAT(p.prenom, ' ', p.nom) AS patient_name, " +
                "p.num_tel AS patient_phone, " +
                "CONCAT(d.prenom, ' ', d.nom) AS doctor_name " +
                "FROM appointment a " +
                "LEFT JOIN utilisateur p ON a.patient_id = p.id " +
                "LEFT JOIN utilisateur d ON a.doctor_id = d.id";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                try {
                    Appointment a = new Appointment();
                    a.setId(rs.getInt("id"));
                    a.setPatientId(rs.getInt("patient_id"));
                    a.setPatientName(rs.getString("patient_name") != null ? rs.getString("patient_name") : "Utilisateur inconnu");
                    String patientPhone = rs.getString("patient_phone");
                    a.setPatientPhone(formatPhoneNumber(patientPhone));
                    if (patientPhone != null && a.getPatientPhone() == null) {
                        System.err.println("Invalid phone number format for patient ID " + a.getPatientId() + ": " + patientPhone);
                    }
                    a.setDoctorId(rs.getInt("doctor_id"));
                    a.setDoctorName(rs.getString("doctor_name") != null ? rs.getString("doctor_name") : "Médecin inconnu");
                    a.setDate(rs.getString("date"));
                    a.setType(rs.getString("type"));
                    a.setStatus(rs.getString("statut"));
                    appointments.add(a);
                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping invalid appointment record: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsByDoctorId(int doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, " +
                "CONCAT(p.prenom, ' ', p.nom) AS patient_name, " +
                "p.num_tel AS patient_phone, " +
                "CONCAT(d.prenom, ' ', d.nom) AS doctor_name " +
                "FROM appointment a " +
                "LEFT JOIN utilisateur p ON a.patient_id = p.id " +
                "LEFT JOIN utilisateur d ON a.doctor_id = d.id " +
                "WHERE a.doctor_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, doctorId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    try {
                        Appointment a = new Appointment();
                        a.setId(rs.getInt("id"));
                        a.setPatientId(rs.getInt("patient_id"));
                        a.setPatientName(rs.getString("patient_name") != null ? rs.getString("patient_name") : "Utilisateur inconnu");
                        String patientPhone = rs.getString("patient_phone");
                        a.setPatientPhone(formatPhoneNumber(patientPhone));
                        if (patientPhone != null && a.getPatientPhone() == null) {
                            System.err.println("Invalid phone number format for patient ID " + a.getPatientId() + ": " + patientPhone);
                        }
                        a.setDoctorId(rs.getInt("doctor_id"));
                        a.setDoctorName(rs.getString("doctor_name") != null ? rs.getString("doctor_name") : "Médecin inconnu");
                        a.setDate(rs.getString("date"));
                        a.setType(rs.getString("type"));
                        a.setStatus(rs.getString("statut"));
                        appointments.add(a);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping invalid appointment record: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture rendez-vous par docteur: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsByConnectedUser() {
        List<Appointment> appointments = new ArrayList<>();
        Utilisateur connectedUser = AuthService.getConnectedUtilisateur();
        if (connectedUser == null) {
            System.err.println("Aucun utilisateur connecté.");
            return appointments;
        }

        String sql;
        int userId = connectedUser.getId();
        int roleId = connectedUser.getRole().getId();

        if (roleId == 3) {
            sql = "SELECT a.*, " +
                    "CONCAT(p.prenom, ' ', p.nom) AS patient_name, " +
                    "p.num_tel AS patient_phone, " +
                    "CONCAT(d.prenom, ' ', d.nom) AS doctor_name " +
                    "FROM appointment a " +
                    "LEFT JOIN utilisateur p ON a.patient_id = p.id " +
                    "LEFT JOIN utilisateur d ON a.doctor_id = d.id " +
                    "WHERE a.patient_id = ?";
        } else if (roleId == 2) {
            sql = "SELECT a.*, " +
                    "CONCAT(p.prenom, ' ', p.nom) AS patient_name, " +
                    "p.num_tel AS patient_phone, " +
                    "CONCAT(d.prenom, ' ', d.nom) AS doctor_name " +
                    "FROM appointment a " +
                    "LEFT JOIN utilisateur p ON a.patient_id = p.id " +
                    "LEFT JOIN utilisateur d ON a.doctor_id = d.id " +
                    "WHERE a.doctor_id = ?";
        } else {
            System.out.println("Rôle non autorisé à voir les rendez-vous: " + roleId);
            return appointments;
        }

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    try {
                        Appointment a = new Appointment();
                        a.setId(rs.getInt("id"));
                        a.setPatientId(rs.getInt("patient_id"));
                        a.setPatientName(rs.getString("patient_name") != null ? rs.getString("patient_name") : "Utilisateur inconnu");
                        String patientPhone = rs.getString("patient_phone");
                        a.setPatientPhone(formatPhoneNumber(patientPhone));
                        if (patientPhone != null && a.getPatientPhone() == null) {
                            System.err.println("Invalid phone number format for patient ID " + a.getPatientId() + ": " + patientPhone);
                        }
                        a.setDoctorId(rs.getInt("doctor_id"));
                        a.setDoctorName(rs.getString("doctor_name") != null ? rs.getString("doctor_name") : "Médecin inconnu");
                        a.setDate(rs.getString("date"));
                        a.setType(rs.getString("type"));
                        a.setStatus(rs.getString("statut"));
                        appointments.add(a);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping invalid appointment record: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture rendez-vous pour utilisateur connecté: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    public boolean deleteAppointment(int id) {
        String sql = "DELETE FROM appointment WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression rendez-vous: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Appointment getAppointmentById(int id) {
        String sql = "SELECT a.*, " +
                "CONCAT(p.prenom, ' ', p.nom) AS patient_name, " +
                "p.num_tel AS patient_phone, " +
                "CONCAT(d.prenom, ' ', d.nom) AS doctor_name " +
                "FROM appointment a " +
                "LEFT JOIN utilisateur p ON a.patient_id = p.id " +
                "LEFT JOIN utilisateur d ON a.doctor_id = d.id " +
                "WHERE a.id=?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Appointment a = new Appointment();
                    a.setId(rs.getInt("id"));
                    a.setPatientId(rs.getInt("patient_id"));
                    a.setPatientName(rs.getString("patient_name") != null ? rs.getString("patient_name") : "Utilisateur inconnu");
                    String patientPhone = rs.getString("patient_phone");
                    a.setPatientPhone(formatPhoneNumber(patientPhone));
                    if (patientPhone != null && a.getPatientPhone() == null) {
                        System.err.println("Invalid phone number format for patient ID " + a.getPatientId() + ": " + patientPhone);
                    }
                    a.setDoctorId(rs.getInt("doctor_id"));
                    a.setDoctorName(rs.getString("doctor_name") != null ? rs.getString("doctor_name") : "Médecin inconnu");
                    a.setDate(rs.getString("date"));
                    a.setType(rs.getString("type"));
                    a.setStatus(rs.getString("statut"));
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}