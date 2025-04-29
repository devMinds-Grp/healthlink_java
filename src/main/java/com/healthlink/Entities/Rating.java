package com.healthlink.Entities;

import java.sql.Timestamp;

public class Rating {
    private int id;
    private int forumId;
    private int userId;
    private int stars;
    private String comment;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Relations optionnelles
    private Forum forum;
    private User user;

    // Constructeurs
    public Rating() {}

    public Rating(int forumId, int userId, int stars, String comment) {
        this.forumId = forumId;
        this.userId = userId;
        this.stars = stars;
        this.comment = comment;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getForumId() { return forumId; }
    public void setForumId(int forumId) { this.forumId = forumId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStars() { return stars; }
    public void setStars(int stars) {
        this.stars = Math.max(1, Math.min(5, stars));
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Méthodes pour les relations
    public Forum getForum() { return forum; }
    public void setForum(Forum forum) {
        this.forum = forum;
        if (forum != null) this.forumId = forum.getId();
    }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) this.userId = user.getId();
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", forumId=" + forumId +
                ", userId=" + userId +
                ", stars=" + stars +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}