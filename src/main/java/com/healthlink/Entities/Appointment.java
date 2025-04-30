package com.healthlink.Entities;

public class Appointment {
    private int id;
    private String date;
    private String type;
    private String status;

    public Appointment() {
    }

    public Appointment(String date, String type) {
        this.date = date;
        this.type = type;
        this.status = "en attente";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Statut invalide: le statut ne peut pas être null");
        }
        String normalizedStatus = status.toLowerCase().trim();
        if (normalizedStatus.equals("en attente") || normalizedStatus.equals("en_attente")) {
            this.status = "en attente";
        } else if (normalizedStatus.equals("confirmé") || normalizedStatus.equals("confirme")) {
            this.status = "confirmé";
        } else if (normalizedStatus.equals("annulé") || normalizedStatus.equals("annule")) {
            this.status = "annulé";
        } else {
            throw new IllegalArgumentException("Statut invalide: " + status);
        }
    }
}