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
    }}



