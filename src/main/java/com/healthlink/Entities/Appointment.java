package com.healthlink.Entities;

public class Appointment {
    private int id;
    private int patientId;
    private String patientName;
    private String patientPhone;
    private int doctorId;
    private String doctorName;
    private String date;
    private String type;
    private AppointmentStatus status;

    public Appointment() {
        this.status = AppointmentStatus.EN_ATTENTE;
    }

    public Appointment(String date, String type) {
        this.date = date;
        this.type = type;
        this.status = AppointmentStatus.EN_ATTENTE;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = AppointmentStatus.fromString(status);
    }
}