package com.healthlink.Services;

import com.healthlink.Entities.Forum;
import com.healthlink.Entities.Rating;
import com.healthlink.Entities.User;
import com.healthlink.Interfaces.InterfaceCRUD;
import com.healthlink.Interfaces.ForumOperations;
import com.healthlink.utils.MyDB;
import javafx.collections.FXCollections;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumService implements InterfaceCRUD<Forum>, ForumOperations {
    private Connection con;

    public ForumService() {
        try {
            con = MyDB.getInstance().getConnection();
            if (con == null) {
                throw new RuntimeException("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
            throw new RuntimeException("Échec de l'initialisation de la connexion", e);
        }
    }

    @Override
    public void add(Forum forum) {
        String req = "INSERT INTO `forum` (`title`, `description`, `date`, `user_id`, `is_approved`) VALUES (?, ?, ?, ?, ?)";
        System.out.println("Ajout du forum: " + forum);
        System.out.println("Paramètres: title=" + forum.getTitle() + ", description=" + forum.getDescription() +
                ", date=" + forum.getDate() + ", user_id=" + forum.getUserId() + ", is_approved=" + forum.isApproved());

        try (PreparedStatement pst = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            String title = forum.getTitle();
            if (title.length() > 255) {
                title = title.substring(0, 255);
            }
            pst.setString(1, title);
            pst.setString(2, forum.getDescription());
            pst.setDate(3, new java.sql.Date(forum.getDate().getTime()));
            pst.setInt(4, forum.getUserId());
            pst.setBoolean(5, forum.isApproved());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        forum.setId(generatedKeys.getInt(1));
                        System.out.println("Forum inséré avec ID: " + forum.getId());
                    }
                }
            } else {
                throw new RuntimeException("Échec de l'insertion du forum, aucune ligne affectée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'ajout du forum: " + e.getMessage());
            throw new RuntimeException("Échec de l'insertion du forum: " + e.getMessage(), e);
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
            String deleteResponsesQuery = "DELETE FROM forum_response WHERE forum_id = ?";
            try (PreparedStatement pst = con.prepareStatement(deleteResponsesQuery)) {
                pst.setInt(1, forum.getId());
                pst.executeUpdate();
            }

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

    public void deleteForum(int forumId) {
        try {
            String deleteResponsesQuery = "DELETE FROM forum_response WHERE forum_id = ?";
            try (PreparedStatement pst = con.prepareStatement(deleteResponsesQuery)) {
                pst.setInt(1, forumId);
                pst.executeUpdate();
            }

            String deleteRatingsQuery = "DELETE FROM rating WHERE forum_id = ?";
            try (PreparedStatement pst = con.prepareStatement(deleteRatingsQuery)) {
                pst.setInt(1, forumId);
                pst.executeUpdate();
            }

            String deleteReportsQuery = "DELETE FROM report WHERE forum_id = ?";
            try (PreparedStatement pst = con.prepareStatement(deleteReportsQuery)) {
                pst.setInt(1, forumId);
                pst.executeUpdate();
            }

            String deleteForumQuery = "DELETE FROM forum WHERE id = ?";
            try (PreparedStatement pst = con.prepareStatement(deleteForumQuery)) {
                pst.setInt(1, forumId);
                int affectedRows = pst.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Forum avec ID " + forumId + " supprimé avec succès.");
                } else {
                    System.out.println("Aucun forum trouvé avec l'ID " + forumId + ".");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du forum: " + e.getMessage());
            throw new RuntimeException("Échec de la suppression du forum", e);
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
        String req = "SELECT f.*, COALESCE(AVG(r.stars), 0) as average_rating " +
                "FROM `forum` f " +
                "LEFT JOIN `rating` r ON f.id = r.forum_id " +
                "WHERE f.is_approved = true " +
                "GROUP BY f.id " +
                "ORDER BY average_rating DESC, f.date DESC";

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
                f.setRatings(FXCollections.observableArrayList(findRatingsByForum(f.getId())));
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

    public void addRating(int forumId, int userId, int stars, String comment) {
        String req = "INSERT INTO rating (forum_id, user_id, stars, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, forumId);
            pst.setInt(2, userId);
            pst.setInt(3, stars);
            pst.setString(4, comment);

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du rating: " + e.getMessage());
            throw new RuntimeException("Échec de l'ajout de l'évaluation", e);
        }
    }

    public List<Rating> findRatingsByForum(int forumId) {
        List<Rating> ratings = new ArrayList<>();
        String req = "SELECT r.*, u.nom, u.prenom FROM `rating` r " +
                "JOIN `utilisateur` u ON r.user_id = u.id " +
                "WHERE r.`forum_id` = ? ORDER BY r.id DESC";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Rating rating = new Rating();
                    rating.setId(rs.getInt("id"));
                    rating.setForumId(forumId);
                    rating.setUserId(rs.getInt("user_id"));
                    rating.setStars(rs.getInt("stars"));
                    rating.setComment(rs.getString("comment"));

                    Forum forum = new Forum();
                    forum.setId(forumId);
                    rating.setForum(forum);

                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    rating.setUser(user);

                    ratings.add(rating);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des ratings: " + e.getMessage());
            throw new RuntimeException("Échec de la récupération des évaluations", e);
        }
        return ratings;
    }

    public double getAverageRating(int forumId) {
        String req = "SELECT AVG(stars) as average FROM `rating` WHERE `forum_id` = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("average");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul de la moyenne: " + e.getMessage());
        }
        return 0;
    }

    public boolean hasUserRatedForum(int forumId, int userId) {
        String req = "SELECT COUNT(*) as count FROM `rating` WHERE `forum_id` = ? AND `user_id` = ?";
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

    public void updateRating(Rating rating) {
        String req = "UPDATE `rating` SET `stars` = ?, `comment` = ? WHERE `id` = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, rating.getStars());
            pst.setString(2, rating.getComment());
            pst.setInt(3, rating.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du rating: " + e.getMessage());
            throw new RuntimeException("Échec de la mise à jour de l'évaluation", e);
        }
    }
}
