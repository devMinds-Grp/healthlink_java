package com.healthlink.Entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Forum {
    private int id;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private Date date;
    private int userId;
    private boolean isApproved;

    // Constructeurs
    public Forum() {}

    public Forum(String title, String description, Date date, int userId, boolean isApproved) {
        this.title.set(title);
        this.description.set(description);
        this.date = date;
        this.userId = userId;
        this.isApproved = isApproved;
    }

    // Getters/Setters standards
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    // Propriétés JavaFX pour TableView
    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty formattedDateProperty() {
        return new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date));
    }

    public StringProperty statusProperty() {
        return new SimpleStringProperty(isApproved ? "Approuvé" : "En attente");
    }

    // Méthode toString()
    @Override
    public String toString() {
        return "Forum{" +
                "id=" + id +
                ", title=" + title.get() +
                ", description=" + description.get() +
                ", date=" + date +
                ", userId=" + userId +
                ", isApproved=" + isApproved +
                '}';
    }

    // Méthodes equals() et hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Forum forum = (Forum) o;
        return id == forum.id;
    }


    public String getFormattedDate() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}