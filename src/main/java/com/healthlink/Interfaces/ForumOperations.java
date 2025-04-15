package com.healthlink.Interfaces;

import com.healthlink.Entities.Forum;
import java.util.List;

public interface ForumOperations {
    // Approuver une publication
    void approveForum(int forumId);

    // Trouver les publications approuvées
    List<Forum> findApprovedForums();

    // Trouver les publications d'un utilisateur (avec pagination)
    List<Forum> findByUser(int userId, int limit, int offset);

    // Recherche par titre ou description
    List<Forum> searchForums(String keyword);

    // Compter le nombre de commentaires pour un forum
    int countResponsesForForum(int forumId);

    // Désapprouver une publication
    void disapproveForum(int forumId);
}
