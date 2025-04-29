package com.healthlink.Entities;

import java.util.Date;

public class Report {
    private int id;
    private int forumId;
    private int reportedById; // correspond à user_id dans votre table
    private Date reportedAt; // correspond à reported_at dans votre table
    private String reason;

    // Constructeurs
    public Report() {}

    public Report(int forumId, int reportedById, Date reportedAt, String reason) {
        this.forumId = forumId;
        this.reportedById = reportedById;
        this.reportedAt = reportedAt;
        this.reason = reason;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getForumId() { return forumId; }
    public void setForumId(int forumId) { this.forumId = forumId; }
    public int getReportedById() { return reportedById; }
    public void setReportedById(int reportedById) { this.reportedById = reportedById; }
    public Date getReportedAt() { return reportedAt; }
    public void setReportedAt(Date reportedAt) { this.reportedAt = reportedAt; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}