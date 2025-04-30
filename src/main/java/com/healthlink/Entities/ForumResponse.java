package com.healthlink.Entities;

import java.util.Date;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.text.SimpleDateFormat;

public class ForumResponse {
    private int id;
    private String description;
    private Date date;
    private int forumId;

    // Constructeurs
    public ForumResponse() {}

    public ForumResponse(String description, Date date, int forumId) {
        this.description = description;
        this.date = date;
        this.forumId = forumId;
    }

    // Getters et Setters (supprimez tout ce qui concerne userId)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getForumId() { return forumId; }
    public void setForumId(int forumId) { this.forumId = forumId; }

    // Propriétés JavaFX
    public StringProperty descriptionProperty() {
        return new SimpleStringProperty(description);
    }

    public StringProperty formattedDateProperty() {
        return new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date));
    }
}