package com.healthlink.Services;

import com.healthlink.Entites.Role;
import com.healthlink.Entities.Care;
import com.healthlink.Entities.Notification;
import com.healthlink.Entites.Utilisateur;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final Connection connection;

    public NotificationService(Connection connection) {
        this.connection = connection;
    }

    public boolean addNotification(Notification notification) {
        String sql = "INSERT INTO notification (user_id, message, created_at, is_read, soignant_id, patient_id, chat_message_id, care_response_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // user_id (recipient of the notification, e.g., soignant for chat messages)
                Utilisateur user = notification.getUser();
                if (user == null || user.getId() == 0) {
                    System.err.println("Cannot add notification: user_id is null or invalid");
                    return false;
                }
                pst.setInt(1, user.getId());
                // message
                pst.setString(2, notification.getMessage());
                // created_at
                pst.setTimestamp(3, Timestamp.valueOf(notification.getCreatedAt()));
                // is_read
                pst.setBoolean(4, notification.isRead());
                // soignant_id (for chat messages, this is the recipient)
                Utilisateur soignant = notification.getUser();
                if (soignant != null && soignant.getRole().getId() == 4) {
                    pst.setInt(5, soignant.getId());
                } else {
                    pst.setNull(5, Types.INTEGER);
                }
                // patient_id (sender of the chat message)
                Utilisateur sender = AuthService.getConnectedUtilisateur();
                if (sender != null && sender.getRole().getId() == 3) {
                    pst.setInt(6, sender.getId());
                } else {
                    pst.setNull(6, Types.INTEGER);
                }
                // chat_message_id (for chat notifications)
                if (notification instanceof ChatNotification) {
                    ChatNotification chatNotification = (ChatNotification) notification;
                    Integer chatMessageId = chatNotification.getChatMessageId();
                    if (chatMessageId != null) {
                        pst.setInt(7, chatMessageId);
                    } else {
                        pst.setNull(7, Types.INTEGER);
                    }
                } else {
                    pst.setNull(7, Types.INTEGER);
                }
                // care_response_id (not used for chat notifications)
                Care care = notification.getCare();
                if (care != null) {
                    String careResponseSql = "SELECT id FROM care_response WHERE care_id = ? ORDER BY date_rep DESC LIMIT 1";
                    try (PreparedStatement careStmt = connection.prepareStatement(careResponseSql)) {
                        careStmt.setInt(1, care.getId());
                        ResultSet rs = careStmt.executeQuery();
                        if (rs.next()) {
                            pst.setInt(8, rs.getInt("id"));
                        } else {
                            pst.setNull(8, Types.INTEGER);
                        }
                    }
                } else {
                    pst.setNull(8, Types.INTEGER);
                }

                int affectedRows = pst.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notification.setId(generatedKeys.getInt(1));
                        connection.commit();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Error adding notification: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
        return false;
    }

    public List<Notification> getNotificationsByUserId(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT n.id, n.message, n.created_at, n.is_read, n.soignant_id, n.patient_id, n.chat_message_id, n.care_response_id, n.user_id, " +
                "u.id AS user_id, u.prenom, u.nom, u.imageprofile, u.role_id " +
                "FROM notification n " +
                "LEFT JOIN utilisateur u ON n.user_id = u.id " +
                "WHERE n.user_id = ? AND n.chat_message_id IS NOT NULL " +
                "ORDER BY n.created_at DESC";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Notification notification = new ChatNotification();
                notification.setId(rs.getInt("id"));
                notification.setMessage(rs.getString("message"));
                notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                notification.setRead(rs.getBoolean("is_read"));

                int userIdResult = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    Utilisateur user = new Utilisateur();
                    user.setId(userIdResult);
                    user.setPrenom(rs.getString("prenom"));
                    user.setNom(rs.getString("nom"));
                    user.setImageprofile(rs.getString("imageprofile"));
                    Role role = new Role();
                    role.setId(rs.getInt("role_id"));
                    user.setRole(role);
                    notification.setUser(user);
                }

                // Set chat_message_id for ChatNotification
                int chatMessageId = rs.getInt("chat_message_id");
                if (!rs.wasNull()) {
                    ((ChatNotification) notification).setChatMessageId(chatMessageId);
                }

                notifications.add(notification);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notifications: " + e.getMessage());
        }
        return notifications;
    }

    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notification SET is_read = TRUE WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, notificationId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            return false;
        }
    }
}

class ChatNotification extends Notification {
    private Integer chatMessageId;

    public ChatNotification() {
        super(null, null);
    }

    public Integer getChatMessageId() { return chatMessageId; }
    public void setChatMessageId(Integer chatMessageId) { this.chatMessageId = chatMessageId; }
}