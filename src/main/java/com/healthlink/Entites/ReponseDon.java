package com.healthlink.Entites;

public class ReponseDon {
    private int id;
    private String description;
    private int bloodDonationId; // Changed from donationId

    // Default constructor
    public ReponseDon() {
    }

    // Constructor for adding a new response
    public ReponseDon(String description, int bloodDonationId) {
        this.description = description;
        this.bloodDonationId = bloodDonationId;
    }

    // Constructor with ID (for retrieving from database)
    public ReponseDon(int id, String description, int bloodDonationId) {
        this.id = id;
        this.description = description;
        this.bloodDonationId = bloodDonationId;
    }

    // Getters and Setters
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

    public int getBloodDonationId() {
        return bloodDonationId;
    }

    public void setBloodDonationId(int bloodDonationId) {
        this.bloodDonationId = bloodDonationId;
    }

    @Override
    public String toString() {
        return "ReponseDon{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", bloodDonationId=" + bloodDonationId +
                '}';
    }
}