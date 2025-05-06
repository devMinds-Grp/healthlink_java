package com.healthlink.Services;

import com.healthlink.Entites.Role;
import com.healthlink.Entities.ChatMessage;
import com.healthlink.Entities.Notification;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    private Connection connection;
    private NotificationService notificationService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatService() {
        try {
            this.connection = MyDB.getInstance().getConnection();
            this.notificationService = new NotificationService(connection);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ChatService", e);
        }
    }

    public void sendMessage(ChatMessage message) {
        try {
            // Encrypt the message (HIPAA compliance)
            String encryptedMessage = java.util.Base64.getEncoder().encodeToString(message.getMessage().getBytes());

            // Insert the chat message and get the generated ID
            String sql = "INSERT INTO chat_messages (sender_id, recipient_id, message, created_at) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, encryptedMessage);
            stmt.setString(4, message.getCreatedAt().format(formatter));
            stmt.executeUpdate();
            System.out.println("Chat message sent: " + message);

            // Get the generated chat_message_id
            int chatMessageId;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    chatMessageId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get chat_message_id");
                }
            }

            // Create a notification for the soignant
            Utilisateur soignant = new Utilisateur();
            soignant.setId(message.getReceiverId());
            Role soignantRole = new Role();
            soignantRole.setId(4); // Soignant role
            soignant.setRole(soignantRole);

            String notificationMessage = "New message from patient (ID: " + message.getSenderId() + "): " + message.getMessage();
            ChatNotification notification = new ChatNotification();
            notification.setUser(soignant);
            notification.setMessage(notificationMessage);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            notification.setChatMessageId(chatMessageId);

            notificationService.addNotification(notification);
        } catch (Exception e) {
            System.err.println("Error sending chat message: " + e.getMessage());
            throw new RuntimeException("Failed to send chat message", e);
        }
    }

    public List<ChatMessage> getMessages(int userId1, int userId2) {
        List<ChatMessage> messages = new ArrayList<>();
        try {
            String sql = "SELECT * FROM chat_messages WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?) ORDER BY created_at ASC";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ChatMessage message = new ChatMessage(
                        rs.getInt("sender_id"),
                        rs.getInt("recipient_id"),
                        new String(java.util.Base64.getDecoder().decode(rs.getString("message")))
                );
                message.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), formatter));
                messages.add(message);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving chat messages: " + e.getMessage());
        }
        return messages;
    }

    public Utilisateur getUserById(int userId) {
        String sql = "SELECT u.id, u.prenom, u.nom, u.num_tel, u.email, u.imageprofile, u.role_id " +
                "FROM utilisateur u WHERE u.id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setPrenom(rs.getString("prenom"));
                user.setNom(rs.getString("nom"));
                user.setNum_tel(rs.getInt("num_tel"));
                user.setEmail(rs.getString("email"));
                user.setImageprofile(rs.getString("imageprofile"));
                Role role = new Role();
                role.setId(rs.getInt("role_id"));
                user.setRole(role);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch user by ID " + userId + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch user by ID", e);
        }
        return null;
    }
}