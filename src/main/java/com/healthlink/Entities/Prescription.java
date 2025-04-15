package com.healthlink.Entities;

public class Prescription {
    private int id;
    private String nomMedicament;
    private String dosage;
    private String duree;
    private String notes;
    private Integer rdvCardId; // Changed from String appointmentDate to Integer rdvCardId

    public Prescription() {
    }

    public Prescription(String nomMedicament, String dosage, String duree, String notes, Integer rdvCardId) {
        this.nomMedicament = nomMedicament;
        this.dosage = dosage;
        this.duree = duree;
        this.notes = notes;
        this.rdvCardId = rdvCardId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public void setNomMedicament(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDuree() {
        return duree;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getRdvCardId() { // Changed from getAppointmentDate
        return rdvCardId;
    }

    public void setRdvCardId(Integer rdvCardId) { // Changed from setAppointmentDate
        this.rdvCardId = rdvCardId;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", nomMedicament='" + nomMedicament + '\'' +
                ", dosage='" + dosage + '\'' +
                ", duree='" + duree + '\'' +
                ", notes='" + notes + '\'' +
                ", rdvCardId=" + rdvCardId +
                '}';
    }
}