package com.healthlink.Entities;

import com.healthlink.Entites.Utilisateur;
import java.time.LocalDateTime;

public class Notification {
    private int id;
    private Utilisateur user;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
    private Care care;

    // Constructor to initialize user and message
    public Notification(Utilisateur user, String message) {
        this.user = user;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.care = null;
    }

    // Default constructor
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Utilisateur getUser() { return user; }
    public void setUser(Utilisateur user) { this.user = user; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Care getCare() { return care; }
    public void setCare(Care care) { this.care = care; }
}