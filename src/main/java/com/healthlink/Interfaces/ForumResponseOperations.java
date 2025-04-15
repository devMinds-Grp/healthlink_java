package com.healthlink.Interfaces;

import com.healthlink.Entities.ForumResponse;
import java.util.List;

public interface ForumResponseOperations {
    // Trouver les réponses d'un forum (avec pagination)
    List<ForumResponse> findByForum(int forumId, int limit, int offset);

    // Trouver les réponses d'un utilisateur
    List<ForumResponse> findByUser(int userId);

    // Supprimer toutes les réponses d'un forum
    void deleteAllResponsesForForum(int forumId);

    // Mettre à jour seulement le texte d'une réponse
    void updateResponseText(int responseId, String newText);
}