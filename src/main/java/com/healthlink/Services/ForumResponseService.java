package com.healthlink.Services;

import com.healthlink.Entities.ForumResponse;
import com.healthlink.Interfaces.InterfaceCRUD;
import com.healthlink.Interfaces.ForumResponseOperations;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumResponseService implements InterfaceCRUD<ForumResponse>, ForumResponseOperations {
    private Connection con;

    public ForumResponseService() {
        try {
            con = MyDB.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
        }
    }

    @Override
    public void add(ForumResponse response) {
        String req = "INSERT INTO `forum_response` (`description`, `date`, `forum_id`) VALUES (?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, response.getDescription());
            pst.setDate(2, new java.sql.Date(response.getDate().getTime()));
            pst.setInt(3, response.getForumId());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        response.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @Override
    public void update(ForumResponse response) {
        String req = "UPDATE `forum_response` SET `description`=?, `date`=? WHERE `id`=?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setString(1, response.getDescription());
            pst.setDate(2, new java.sql.Date(response.getDate().getTime()));
            pst.setInt(3, response.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @Override
    public void delete(ForumResponse response) {
        String req = "DELETE FROM `forum_response` WHERE `id`=?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, response.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public List<ForumResponse> find() {
        List<ForumResponse> responses = new ArrayList<>();
        String req = "SELECT * FROM `forum_response` ORDER BY `date` DESC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                ForumResponse fr = new ForumResponse();
                fr.setId(rs.getInt("id"));
                fr.setDescription(rs.getString("description"));
                fr.setDate(rs.getDate("date"));
                fr.setForumId(rs.getInt("forum_id"));
                responses.add(fr);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return responses;
    }

    public List<ForumResponse> findAllCommentsByForumId(int forumId) {
        List<ForumResponse> responses = new ArrayList<>();
        String req = "SELECT fr.* FROM forum_response fr WHERE fr.forum_id = ? ORDER BY fr.date DESC";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId); // Paramètre pour filtrer par forum_id

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ForumResponse fr = new ForumResponse();
                    fr.setId(rs.getInt("id"));
                    fr.setDescription(rs.getString("description"));
                    fr.setDate(rs.getDate("date"));
                    fr.setForumId(rs.getInt("forum_id"));
                    responses.add(fr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commentaires du forum: " + e.getMessage());
        }
        return responses;
    }

    @Override
    public List<ForumResponse> findByForum(int forumId, int limit, int offset) {
        List<ForumResponse> responses = new ArrayList<>();
        String req = "SELECT * FROM `forum_response` WHERE `forum_id`=? ORDER BY `date` DESC LIMIT ? OFFSET ?";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);
            pst.setInt(2, limit);
            pst.setInt(3, offset);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ForumResponse fr = new ForumResponse();
                    fr.setId(rs.getInt("id"));
                    fr.setDescription(rs.getString("description"));
                    fr.setDate(rs.getDate("date"));
                    fr.setForumId(forumId);
                    responses.add(fr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
        }
        return responses;
    }

    @Override
    public List<ForumResponse> findByUser(int userId) {
        List<ForumResponse> responses = new ArrayList<>();
        String req = "SELECT * FROM `forum_response` WHERE `user_id`=? ORDER BY `date` DESC";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, userId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ForumResponse fr = new ForumResponse();
                    fr.setId(rs.getInt("id"));
                    fr.setDescription(rs.getString("description"));
                    fr.setDate(rs.getDate("date"));
                    fr.setForumId(rs.getInt("forum_id"));
                    responses.add(fr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
        }
        return responses;
    }

    @Override
    public void deleteAllResponsesForForum(int forumId) {
        String req = "DELETE FROM `forum_response` WHERE `forum_id`=?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public void updateResponseText(int responseId, String newText) {
        String req = "UPDATE `forum_response` SET `description`=? WHERE `id`=?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setString(1, newText);
            pst.setInt(2, responseId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    public boolean isOwner(int responseId, int userId) {
        String req = "SELECT user_id FROM forum_response WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, responseId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id") == userId;
            }
        } catch (SQLException e) {
            System.err.println("Erreur de vérification de propriété: " + e.getMessage());
        }
        return false;
    }
}