package com.healthlink.Entities;
import java.time.LocalDate;

public class CareResponse {
    private int id;
    private LocalDate dateRep;
    private String contenuRep;
    private Care care;

    // Constructors
    public CareResponse() {
        this.dateRep = LocalDate.now();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDateRep() { return dateRep; }
    public void setDateRep(LocalDate dateRep) { this.dateRep = dateRep; }

    public String getContenuRep() { return contenuRep; }
    public void setContenuRep(String contenuRep) { this.contenuRep = contenuRep; }

    // Relationship methods
    public Care getCare() { return care; }
    public void setCare(Care care) { this.care = care; }

    @Override
    public String toString() {
        return String.format("CareResponse{id=%d, dateRep=%s, contenuRep='%s'}",
                id, dateRep, contenuRep);
    }
}