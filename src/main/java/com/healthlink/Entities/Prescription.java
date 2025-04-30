package com.healthlink.Entities;

public class Prescription {
    private int id;
    private String nomMedicament;
    private String dosage;
    private String duree;
    private String notes;
    private int rdvCardId;
    private String patientName;
    private String appointmentDate; // Add this field back

    // Default constructor
    public Prescription() {}

    // Constructor with parameters
    public Prescription(String nomMedicament, String dosage, String duree, String notes, Integer rdvCardId) {
        this.nomMedicament = nomMedicament;
        this.dosage = dosage;
        this.duree = duree;
        this.notes = notes;
        this.rdvCardId = (rdvCardId != null) ? rdvCardId : 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomMedicament() { return nomMedicament; }
    public void setNomMedicament(String nomMedicament) { this.nomMedicament = nomMedicament; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getRdvCardId() { return rdvCardId; }
    public void setRdvCardId(int rdvCardId) { this.rdvCardId = rdvCardId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    @Override
    public String toString() {
        return "Prescription{id=" + id + ", nomMedicament='" + nomMedicament + "', dosage='" + dosage +
                "', duree='" + duree + "', notes='" + notes + "', rdvCardId=" + rdvCardId +
                ", patientName='" + patientName + "', appointmentDate='" + appointmentDate + "'}";
    }
}