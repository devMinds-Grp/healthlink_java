package com.healthlink.Services;

import com.healthlink.Entities.Rating;
import com.healthlink.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingService {
    private Connection con;

    public RatingService() {
        try {
            con = MyDB.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
        }
    }

    /**
     * Ajoute une nouvelle évaluation dans la base de données
     */
    public void addRating(Rating rating) {
        String req = "INSERT INTO rating (forum_id, user_id, stars, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, rating.getForumId());
            pst.setInt(2, rating.getUserId());
            pst.setInt(3, rating.getStars());
            pst.setString(4, rating.getComment());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rating.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du rating: " + e.getMessage());
            throw new RuntimeException("Échec de l'ajout de l'évaluation", e);
        }
    }

    /**
     * Met à jour une évaluation existante
     */
    public void updateRating(Rating rating) {
        String req = "UPDATE `rating` SET `stars` = ?, `comment` = ? WHERE `id` = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, rating.getStars());
            pst.setString(2, rating.getComment());
            pst.setInt(3, rating.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Évaluation mise à jour avec succès - ID: " + rating.getId());
            } else {
                System.err.println("Aucune évaluation mise à jour pour l'ID: " + rating.getId());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du rating: " + e.getMessage());
            throw new RuntimeException("Échec de la mise à jour de l'évaluation", e);
        }
    }

    /**
     * Récupère toutes les évaluations d'un forum spécifique
     */
    public List<Rating> findRatingsByForum(int forumId) {
        List<Rating> ratings = new ArrayList<>();
        String req = "SELECT * FROM rating WHERE forum_id = ? ORDER BY id DESC";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Rating rating = mapResultSetToRating(rs);
                    ratings.add(rating);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des ratings: " + e.getMessage());
            throw new RuntimeException("Échec de la récupération des évaluations", e);
        }
        return ratings;
    }

    /**
     * Récupère une évaluation spécifique d'un utilisateur pour un forum
     */
    public Rating findUserRatingForForum(int forumId, int userId) {
        String req = "SELECT * FROM rating WHERE forum_id = ? AND user_id = ? LIMIT 1";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, forumId);
            pst.setInt(2, userId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRating(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du rating utilisateur: " + e.getMessage());
        }
        return null;
    }

    /**
     * Calcule la note moyenne d'un forum
     */
    public double getAverageRating(int forumId) {
        String req = "SELECT AVG(stars) as average FROM rating WHERE forum_id = ?";
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

    /**
     * Vérifie si un utilisateur a déjà noté un forum
     */
    public boolean hasUserRatedForum(int forumId, int userId) {
        String req = "SELECT COUNT(*) as count FROM rating WHERE forum_id = ? AND user_id = ?";
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

    /**
     * Supprime une évaluation
     */
    public void deleteRating(int ratingId) {
        String req = "DELETE FROM rating WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, ratingId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du rating: " + e.getMessage());
            throw new RuntimeException("Échec de la suppression de l'évaluation", e);
        }
    }

    /**
     * Méthode utilitaire pour mapper un ResultSet à un objet Rating
     */
    private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getInt("id"));
        rating.setForumId(rs.getInt("forum_id"));
        rating.setUserId(rs.getInt("user_id"));
        rating.setStars(rs.getInt("stars"));
        rating.setComment(rs.getString("comment"));
        rating.setCreatedAt(rs.getTimestamp("created_at"));

        // Gestion optionnelle de updated_at
        try {
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                rating.setUpdatedAt(updatedAt);
            }
        } catch (SQLException e) {
            // Colonne peut être absente
        }

        return rating;
    }
}