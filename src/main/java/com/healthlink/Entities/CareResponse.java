package com.healthlink.Entities;

import com.healthlink.Entites.Utilisateur;
import java.time.LocalDate;

public class CareResponse {
    private int id;
    private LocalDate dateRep;
    private String contenuRep;
    private Care care;
    private Utilisateur user;
    private Utilisateur patient;
    private Utilisateur soignant;
    private int careId;


    // Constructors
    public CareResponse() {
        this.dateRep = LocalDate.now();
    }
    public int getCareId() {
        return careId;
    }

    public void setCareId(int careId) {
        this.careId = careId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDateRep() { return dateRep; }
    public void setDateRep(LocalDate dateRep) { this.dateRep = dateRep; }

    public String getContenuRep() { return contenuRep; }
    public void setContenuRep(String contenuRep) { this.contenuRep = contenuRep; }

    public Care getCare() { return care; }
    public void setCare(Care care) { this.care = care; }

    public Utilisateur getUser() { return user; }
    public void setUser(Utilisateur user) { this.user = user; }

    public Utilisateur getPatient() { return patient; }
    public void setPatient(Utilisateur patient) { this.patient = patient; }

    public Utilisateur getSoignant() { return soignant; }
    public void setSoignant(Utilisateur soignant) { this.soignant = soignant; }

    @Override
    public String toString() {
        return String.format("CareResponse{id=%d, dateRep=%s, contenuRep='%s', " +
                        "user=%s, patient=%s, soignant=%s}",
                id, dateRep, contenuRep,
                user != null ? user.getPrenom() + " " + user.getNom() : "null",
                patient != null ? patient.getPrenom() + " " + patient.getNom() : "null",
                soignant != null ? soignant.getPrenom() + " " + soignant.getNom() : "null");
    }
}