package com.healthlink.Entites;

import java.time.LocalDate;

public class DonDuSang {
    private int id;
    private String description;
    private String lieu;
    private LocalDate date;
    private String numTel;

    // Constructors
    public DonDuSang() {
        this.date = LocalDate.now();
    }

    public DonDuSang(String description, String lieu, LocalDate date, String numTel) {
        this.description = description;
        this.lieu = lieu;
        this.date = date;
        this.numTel = numTel;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    @Override
    public String toString() {
        return String.format("DonDuSang{id=%d, description='%s', lieu='%s', date=%s, numTel='%s'}",
                id, description, lieu, date, numTel);
    }
}