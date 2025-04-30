package com.healthlink.Entities;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Care {
    private int id;
    private LocalDate date;
    private String address;
    private String description;
    private List<CareResponse> responses = new ArrayList<>();

    // Constructors
    public Care() {
        this.date = LocalDate.now();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Relationship methods
    public List<CareResponse> getResponses() { return responses; }

    public void addResponse(CareResponse response) {
        responses.add(response);
        response.setCare(this);
    }

    public void removeResponse(CareResponse response) {
        responses.remove(response);
        response.setCare(null);
    }

    @Override
    public String toString() {
        return String.format("Care{id=%d, date=%s, address='%s'}", id, date, address);
    }
}