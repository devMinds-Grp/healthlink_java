package com.healthlink.tests;

import com.healthlink.Entities.Forum;
import com.healthlink.Entities.ForumResponse;
import com.healthlink.Services.ForumService;
import com.healthlink.Services.ForumResponseService;
import com.healthlink.utils.MyDB;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class TestForum {
    public static void main(String[] args) {
        try {
            // Test du service Forum
            ForumService forumService = new ForumService();

            // Création d'une publication
            Forum forum = new Forum("Première publication", "Ceci est une description de test", new Date(), 1, false);
            forumService.add(forum);
            System.out.println("Forum ajouté avec l'ID: " + forum.getId());

            // Approbation d'une publication
            forumService.approveForum(forum.getId());
            System.out.println("Forum approuvé");

            // Liste des publications
            List<Forum> forums = forumService.find();
            System.out.println("\nListe des publications:");
            for (Forum f : forums) {
                System.out.println(f);
            }

            // Test du service ForumResponse
            ForumResponseService responseService = new ForumResponseService();

            // Ajout d'un commentaire
            ForumResponse response = new ForumResponse("Ceci est un commentaire", new Date(), 2);
            responseService.add(response);
            System.out.println("\nCommentaire ajouté avec l'ID: " + response.getId());

            // Liste des commentaires pour une publication (avec pagination)
            List<ForumResponse> responses = responseService.findByForum(forum.getId(), 10, 0);
            System.out.println("\nListe des commentaires pour la publication " + forum.getId() + ":");
            for (ForumResponse fr : responses) {
                System.out.println(fr);
            }

            // Test de recherche
            System.out.println("\nRecherche de forums contenant 'test':");
            List<Forum> searchResults = forumService.searchForums("test");
            for (Forum f : searchResults) {
                System.out.println(f);
            }

            // Test des forums approuvés
            System.out.println("\nListe des forums approuvés:");
            List<Forum> approvedForums = forumService.findApprovedForums();
            for (Forum f : approvedForums) {
                System.out.println(f);
            }

            // Test de mise à jour
            forum.setTitle("Titre mis à jour");
            forumService.update(forum);
            System.out.println("\nForum mis à jour");

            // Compter les commentaires
            int responseCount = forumService.countResponsesForForum(forum.getId());
            System.out.println("\nNombre de commentaires pour le forum " + forum.getId() + ": " + responseCount);

            // Test des commentaires par utilisateur
            System.out.println("\nCommentaires de l'utilisateur 2:");
            List<ForumResponse> userResponses = responseService.findByUser(2);
            for (ForumResponse fr : userResponses) {
                System.out.println(fr);
            }

            // Test de mise à jour du texte d'un commentaire
            responseService.updateResponseText(response.getId(), "Commentaire mis à jour");
            System.out.println("\nTexte du commentaire mis à jour");

            // Test de la désapprobation d'un forum
            forumService.disapproveForum(forum.getId());
            System.out.println("\nForum désapprouvé");

            // Test de suppression des commentaires et forums
            // Décommentez pour tester la suppression
            /*
            responseService.delete(response);
            System.out.println("\nCommentaire supprimé");

            forumService.delete(forum);
            System.out.println("Forum supprimé");
            */

            // Test de suppression de tous les commentaires d'un forum
            // Décommentez pour tester
            /*
            responseService.deleteAllResponsesForForum(forum.getId());
            System.out.println("\nTous les commentaires du forum ont été supprimés");
            */

            // Fermer proprement la connexion à la fin des tests
            MyDB.getInstance().closeConnection();

        } catch (Exception e) {
            System.err.println("Erreur lors des tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
}