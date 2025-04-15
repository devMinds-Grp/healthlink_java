package com.healthlink.org.example;

import com.healthlink.Entites.Reclamation;
import com.healthlink.Entites.Categorie;
import com.healthlink.Services.ReclamationService;
import com.healthlink.Services.CategorieService;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ReclamationService reclamationService = new ReclamationService();
    private static final CategorieService categorieService = new CategorieService();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Menu Principal ===");
            System.out.println("1. Gérer les Réclamations");
            System.out.println("2. Gérer les Catégories");
            System.out.println("0. Quitter");
            System.out.print("Choix : ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> manageReclamations();
                case 2 -> manageCategories();
                case 0 -> running = false;
                default -> System.out.println("❌ Option invalide !");
            }
        }

        System.out.println("✅ Application terminée.");
    }

    // ------------------ RÉCLAMATIONS ------------------
    private static void manageReclamations() {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Gestion des Réclamations ---");
            System.out.println("1. Ajouter");
            System.out.println("2. Lister");
            System.out.println("3. Modifier");
            System.out.println("4. Supprimer");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> addReclamation();
                case 2 -> listReclamations();
                case 3 -> updateReclamation();
                case 4 -> deleteReclamation();
                case 0 -> back = true;
                default -> System.out.println("❌ Option invalide !");
            }
        }
    }

    private static void addReclamation() {
        System.out.print("Titre : ");
        String titre = scanner.nextLine();
        System.out.print("Description : ");
        String description = scanner.nextLine();

        List<Categorie> categories = categorieService.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("❌ Aucune catégorie disponible !");
            return;
        }

        System.out.println("\nCatégories disponibles :");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, categories.get(i).getNom());
        }

        System.out.print("Choisir une catégorie (numéro) : ");
        int catChoice = scanner.nextInt();
        scanner.nextLine();

        if (catChoice < 1 || catChoice > categories.size()) {
            System.out.println("❌ Choix invalide.");
            return;
        }

        int categorieId = categories.get(catChoice - 1).getId();
        Reclamation r = new Reclamation(titre, description, categorieId);

        if (reclamationService.addReclamation(r)) {
            System.out.println("✅ Réclamation ajoutée !");
        } else {
            System.out.println("❌ Échec de l'ajout");
        }
    }

    private static void listReclamations() {
        List<Reclamation> list = reclamationService.getAllReclamations();

        if (list.isEmpty()) {
            System.out.println("Aucune réclamation trouvée.");
        } else {
            System.out.println("\n--- Liste des Réclamations ---");
            System.out.println("ID  | Titre            | Catégorie       | Description");
            System.out.println("----|------------------|-----------------|-------------------");

            // Affichage des réclamations
            for (Reclamation r : list) {
                String categorieNom = "Inconnue"; // Par défaut, on met Inconnue pour les catégories inexistantes
                Categorie c = categorieService.getCategorieById(r.getCategorieId()); // Récupérer la catégorie par ID
                if (c != null) {
                    categorieNom = c.getNom(); // Si la catégorie existe, on récupère son nom
                }

                // Afficher les informations de la réclamation (ID, Titre, Catégorie, Description)
                System.out.printf("%-3d | %-15s | %-15s | %-20s%n",
                        r.getId(),
                        r.getTitre(),
                        categorieNom,
                        r.getDescription().length() > 20
                                ? r.getDescription().substring(0, 17) + "..." // Limiter la longueur de la description
                                : r.getDescription());
            }
        }
    }


    private static void updateReclamation() {
        listReclamations();
        System.out.print("\nID de la réclamation à modifier : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Nouveau titre : ");
        String titre = scanner.nextLine();
        System.out.print("Nouvelle description : ");
        String description = scanner.nextLine();

        List<Categorie> categories = categorieService.getAllCategories();
        System.out.println("\nCatégories disponibles :");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, categories.get(i).getNom());
        }

        System.out.print("Choisir une catégorie (numéro) : ");
        int catChoice = scanner.nextInt();
        scanner.nextLine();

        if (catChoice < 1 || catChoice > categories.size()) {
            System.out.println("❌ Choix invalide.");
            return;
        }

        int categorieId = categories.get(catChoice - 1).getId();
        Reclamation r = new Reclamation(id, titre, description, categorieId);

        if (reclamationService.updateReclamation(r)) {
            System.out.println("✅ Mise à jour réussie !");
        } else {
            System.out.println("❌ Échec de la mise à jour");
        }
    }

    private static void deleteReclamation() {
        listReclamations();
        System.out.print("\nID de la réclamation à supprimer : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        if (reclamationService.deleteReclamation(id)) {
            System.out.println("✅ Supprimée !");
        } else {
            System.out.println("❌ Échec de la suppression");
        }
    }

    // ------------------ CATÉGORIES ------------------
    private static void manageCategories() {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Gestion des Catégories ---");
            System.out.println("1. Ajouter");
            System.out.println("2. Lister");
            System.out.println("3. Modifier");
            System.out.println("4. Supprimer");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> addCategorie();
                case 2 -> listCategories();
                case 3 -> updateCategorie();
                case 4 -> deleteCategorie();
                case 0 -> back = true;
                default -> System.out.println("❌ Option invalide !");
            }
        }
    }

    private static void addCategorie() {
        System.out.print("Nom de la catégorie : ");
        String nom = scanner.nextLine();
        Categorie categorie = new Categorie(nom);

        if (categorieService.addCategorie(categorie)) {
            System.out.println("✅ Catégorie ajoutée !");
        } else {
            System.out.println("❌ Échec de l'ajout");
        }
    }

    private static void listCategories() {
        List<Categorie> list = categorieService.getAllCategories();

        if (list.isEmpty()) {
            System.out.println("Aucune catégorie trouvée.");
        } else {
            System.out.println("\n--- Liste des Catégories ---");
            System.out.println("ID  | Nom");
            System.out.println("----|------------------");
            for (Categorie c : list) {
                System.out.printf("%-3d | %-15s%n", c.getId(), c.getNom());
            }
        }
    }

    private static void updateCategorie() {
        listCategories();
        System.out.print("\nID de la catégorie à modifier : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Nouveau nom : ");
        String nom = scanner.nextLine();

        Categorie categorie = new Categorie(id, nom);
        if (categorieService.updateCategorie(categorie)) {
            System.out.println("✅ Mise à jour réussie !");
        } else {
            System.out.println("❌ Échec de la mise à jour");
        }
    }

    private static void deleteCategorie() {
        listCategories();
        System.out.print("\nID de la catégorie à supprimer : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        if (categorieService.deleteCategorie(id)) {
            System.out.println("✅ Supprimée !");
        } else {
            System.out.println("❌ Échec de la suppression");
        }
    }

    // Méthode utilitaire pour tronquer une chaîne
    private static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

}
