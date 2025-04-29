package com.healthlink.Services;

import com.healthlink.Entities.Report;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportService {
    private Connection con;

    public ReportService() {
        try {
            con = MyDB.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
        }
    }

    public void addReport(Report report) {
        String req = "INSERT INTO report (forum_id, reported_by_id, reported_at, reason) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, report.getForumId());
            pst.setInt(2, report.getReportedById());
            pst.setTimestamp(3, new Timestamp(report.getReportedAt().getTime()));
            pst.setString(4, report.getReason());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du signalement: " + e.getMessage());
        }
    }

    public boolean hasUserReportedForum(int forumId, int userId) {
        String req = "SELECT COUNT(*) as count FROM report WHERE forum_id = ? AND reported_by_id = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);
            pst.setInt(2, userId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification: " + e.getMessage());
        }
        return false;
    }

    public int countReportsForForum(int forumId) {
        String req = "SELECT COUNT(*) as count FROM report WHERE forum_id = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des signalements: " + e.getMessage());
        }
        return 0;
    }

    public List<Report> findReportsByForum(int forumId) {
        List<Report> reports = new ArrayList<>();
        String req = "SELECT * FROM report WHERE forum_id = ? ORDER BY reported_at DESC";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Report report = new Report();
                    report.setId(rs.getInt("id"));
                    report.setForumId(rs.getInt("forum_id"));
                    report.setReportedById(rs.getInt("reported_by_id"));
                    report.setReportedAt(new Date(rs.getTimestamp("reported_at").getTime()));
                    report.setReason(rs.getString("reason"));
                    reports.add(report);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des signalements: " + e.getMessage());
        }
        return reports;
    }
}