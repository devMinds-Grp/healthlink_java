package com.healthlink.Entites;

public class ReponseDon {
    private int id;
    private String description;

    // Default constructor
    public ReponseDon() {
    }

    // Constructor for adding a new response
    public ReponseDon(String description) {
        this.description = description;
    }

    // Constructor with ID (for retrieving from database)
    public ReponseDon(int id, String description) {
        this.id = id;
        this.description = description;
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

    @Override
    public String toString() {
        return "ReponseDon{" +
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }
}