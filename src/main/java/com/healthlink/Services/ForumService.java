package com.healthlink.Services;

import com.healthlink.Entities.Forum;
import com.healthlink.Interfaces.InterfaceCRUD;
import com.healthlink.Interfaces.ForumOperations;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumService implements InterfaceCRUD<Forum>, ForumOperations {
    private Connection con;

    public ForumService() {
        try {
            con = MyDB.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
        }
    }

    @Override
    public void add(Forum forum) {
        String req = "INSERT INTO `forum` (`title`, `description`, `date`, `user_id`, `is_approved`) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, forum.getTitle());
            pst.setString(2, forum.getDescription());
            pst.setDate(3, new java.sql.Date(forum.getDate().getTime()));
            pst.setInt(4, forum.getUserId());
            pst.setBoolean(5, forum.isApproved());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        forum.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @Override
    public void update(Forum forum) {
        String req = "UPDATE `forum` SET `title`=?, `description`=?, `date`=?, `is_approved`=? WHERE `id`=?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setString(1, forum.getTitle());
            pst.setString(2, forum.getDescription());
            pst.setDate(3, new java.sql.Date(forum.getDate().getTime()));
            pst.setBoolean(4, forum.isApproved());
            pst.setInt(5, forum.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @Override
    public void delete(Forum forum) {
        String req = "DELETE FROM `forum` WHERE `id`=?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forum.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    public void deleteForumWithResponses(Forum forum) {
        try {
            // 1. Supprimer d'abord les commentaires associés
            String deleteResponsesQuery = "DELETE FROM forum_response WHERE forum_id = ?";
            try (PreparedStatement pst = con.prepareStatement(deleteResponsesQuery)) {
                pst.setInt(1, forum.getId());
                pst.executeUpdate();
            }

            // 2. Puis supprimer le forum
            String deleteForumQuery = "DELETE FROM forum WHERE id = ?";
            try (PreparedStatement pst = con.prepareStatement(deleteForumQuery)) {
                pst.setInt(1, forum.getId());
                pst.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            throw new RuntimeException("Échec de la suppression du forum et de ses commentaires", e);
        }
    }

    @Override
    public List<Forum> find() {
        List<Forum> forums = new ArrayList<>();
        String req = "SELECT * FROM `forum` ORDER BY `date` DESC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Forum f = new Forum();
                f.setId(rs.getInt("id"));
                f.setTitle(rs.getString("title"));
                f.setDescription(rs.getString("description"));
                f.setDate(rs.getDate("date"));
                f.setUserId(rs.getInt("user_id"));
                f.setApproved(rs.getBoolean("is_approved"));
                forums.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return forums;
    }

    public List<Forum> findUnapprovedForums() {
        List<Forum> forums = new ArrayList<>();
        String req = "SELECT * FROM `forum` WHERE `is_approved` = false ORDER BY `date` DESC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Forum f = new Forum();
                f.setId(rs.getInt("id"));
                f.setTitle(rs.getString("title"));
                f.setDescription(rs.getString("description"));
                f.setDate(rs.getDate("date"));
                f.setUserId(rs.getInt("user_id"));
                f.setApproved(false);
                forums.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des forums non approuvés: " + e.getMessage());
        }
        return forums;
    }

    @Override
    public void approveForum(int forumId) {
        String req = "UPDATE `forum` SET `is_approved` = true WHERE `id` = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'approbation du forum: " + e.getMessage());
        }
    }

    @Override
    public void disapproveForum(int forumId) {
        String req = "UPDATE `forum` SET `is_approved` = false WHERE `id` = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désapprobation du forum: " + e.getMessage());
        }
    }

    @Override
    public List<Forum> findApprovedForums() {
        List<Forum> forums = new ArrayList<>();
        String req = "SELECT * FROM `forum` WHERE `is_approved` = true ORDER BY `date` DESC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Forum f = new Forum();
                f.setId(rs.getInt("id"));
                f.setTitle(rs.getString("title"));
                f.setDescription(rs.getString("description"));
                f.setDate(rs.getDate("date"));
                f.setUserId(rs.getInt("user_id"));
                f.setApproved(true);
                forums.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des forums approuvés: " + e.getMessage());
        }
        return forums;
    }

    @Override
    public List<Forum> findByUser(int userId, int limit, int offset) {
        List<Forum> forums = new ArrayList<>();
        String req = "SELECT * FROM `forum` WHERE `user_id` = ? ORDER BY `date` DESC LIMIT ? OFFSET ?";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.setInt(2, limit);
            pst.setInt(3, offset);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Forum f = new Forum();
                    f.setId(rs.getInt("id"));
                    f.setTitle(rs.getString("title"));
                    f.setDescription(rs.getString("description"));
                    f.setDate(rs.getDate("date"));
                    f.setUserId(userId);
                    f.setApproved(rs.getBoolean("is_approved"));
                    forums.add(f);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par utilisateur: " + e.getMessage());
        }
        return forums;
    }

    @Override
    public List<Forum> searchForums(String keyword) {
        List<Forum> forums = new ArrayList<>();
        String req = "SELECT * FROM `forum` WHERE `title` LIKE ? OR `description` LIKE ?";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Forum f = new Forum();
                    f.setId(rs.getInt("id"));
                    f.setTitle(rs.getString("title"));
                    f.setDescription(rs.getString("description"));
                    f.setDate(rs.getDate("date"));
                    f.setUserId(rs.getInt("user_id"));
                    f.setApproved(rs.getBoolean("is_approved"));
                    forums.add(f);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
        }
        return forums;
    }

    @Override
    public int countResponsesForForum(int forumId) {
        String req = "SELECT COUNT(*) FROM `forum_response` WHERE `forum_id` = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des réponses: " + e.getMessage());
        }
        return 0;
    }
    public List<Forum> findAllForums() {
        List<Forum> forums = new ArrayList<>();
        String req = "SELECT * FROM `forum` ORDER BY `is_approved`, `date` DESC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Forum f = new Forum();
                f.setId(rs.getInt("id"));
                f.setTitle(rs.getString("title"));
                f.setDescription(rs.getString("description"));
                f.setDate(rs.getDate("date"));
                f.setUserId(rs.getInt("user_id"));
                f.setApproved(rs.getBoolean("is_approved"));
                forums.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les forums: " + e.getMessage());
        }
        return forums;
    }
}