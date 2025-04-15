package com.healthlink.org.example;

import com.healthlink.Entites.ReponseDon;
import com.healthlink.Entites.DonDuSang;
import com.healthlink.Services.ReponseDonService;
import com.healthlink.Services.DonDuSangService;
import com.healthlink.utils.MyDB;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static ReponseDonService reponseDonService;
    private static DonDuSangService donDuSangService;

    static {
        try {
            MyDB db = MyDB.getInstance();
            reponseDonService = new ReponseDonService(db.getConnection());
            donDuSangService = new DonDuSangService(db.getConnection());
        } catch (SQLException e) {
            System.err.println("Failed to initialize services: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Menu Principal ===");
            System.out.println("1. Gérer les Dons de Sang");
            System.out.println("2. Gérer les Réponses");
            System.out.println("0. Quitter");
            System.out.print("Choix : ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> manageDonsDeSang();
                    case 2 -> manageReponsesDons();
                    case 0 -> running = false;
                    default -> System.out.println("❌ Option invalide !");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Veuillez entrer un nombre valide !");
            }
        }

        System.out.println("✅ Application terminée.");
        scanner.close();
        MyDB.getInstance().closeConnection();
    }

    // ------------------ GESTION DES DONS DE SANG ------------------
    private static void manageDonsDeSang() {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Gestion des Dons de Sang ---");
            System.out.println("1. Ajouter un don");
            System.out.println("2. Lister tous les dons");
            System.out.println("3. Modifier un don");
            System.out.println("4. Supprimer un don");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> addDonDeSang();
                    case 2 -> listDonsDeSang();
                    case 3 -> updateDonDeSang();
                    case 4 -> deleteDonDeSang();
                    case 0 -> back = true;
                    default -> System.out.println("❌ Option invalide !");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Veuillez entrer un nombre valide !");
            }
        }
    }

    private static void addDonDeSang() {
        System.out.print("Description : ");
        String description = scanner.nextLine();

        System.out.print("Lieu : ");
        String lieu = scanner.nextLine();

        System.out.print("Date (YYYY-MM-DD) : ");
        String dateStr = scanner.nextLine();

        System.out.print("Numéro de téléphone : ");
        String numTel = scanner.nextLine();

        try {
            LocalDate date = LocalDate.parse(dateStr);
            DonDuSang don = new DonDuSang(description, lieu, date, numTel);

            if (donDuSangService.addDonDuSang(don)) {
                System.out.println("✅ Don ajouté avec succès !");
            } else {
                System.out.println("❌ Échec de l'ajout du don");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Format de date invalide. Utilisez YYYY-MM-DD");
        }
    }

    private static void listDonsDeSang() {
        List<DonDuSang> dons = donDuSangService.getAllDonDuSang();

        if (dons.isEmpty()) {
            System.out.println("Aucun don de sang trouvé.");
        } else {
            System.out.println("\n--- Liste des Dons de Sang ---");
            System.out.printf("%-5s | %-20s | %-15s | %-12s | %-12s%n",
                    "ID", "Description", "Lieu", "Date", "Téléphone");
            System.out.println("------|----------------------|-----------------|--------------|-------------");

            for (DonDuSang don : dons) {
                System.out.printf("%-5d | %-20s | %-15s | %-12s | %-12s%n",
                        don.getId(),
                        truncate(don.getDescription(), 20),
                        truncate(don.getLieu(), 15),
                        don.getDate(),
                        don.getNumTel());
            }
        }
    }

    private static void updateDonDeSang() {
        listDonsDeSang();
        System.out.print("\nID du don à modifier : ");

        try {
            int id = Integer.parseInt(scanner.nextLine());
            DonDuSang don = donDuSangService.getDonDuSangById(id);

            if (don == null) {
                System.out.println("❌ Aucun don trouvé avec cet ID");
                return;
            }

            System.out.print("Nouvelle description (" + don.getDescription() + ") : ");
            String description = scanner.nextLine();
            if (!description.isEmpty()) don.setDescription(description);

            System.out.print("Nouveau lieu (" + don.getLieu() + ") : ");
            String lieu = scanner.nextLine();
            if (!lieu.isEmpty()) don.setLieu(lieu);

            System.out.print("Nouvelle date (" + don.getDate() + ") (YYYY-MM-DD) : ");
            String dateStr = scanner.nextLine();
            if (!dateStr.isEmpty()) {
                try {
                    don.setDate(LocalDate.parse(dateStr));
                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Format de date invalide. La date n'a pas été modifiée.");
                }
            }

            System.out.print("Nouveau numéro de téléphone (" + don.getNumTel() + ") : ");
            String numTel = scanner.nextLine();
            if (!numTel.isEmpty()) don.setNumTel(numTel);

            if (donDuSangService.updateDonDuSang(don)) {
                System.out.println("✅ Don mis à jour avec succès !");
            } else {
                System.out.println("❌ Échec de la mise à jour du don");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un ID valide !");
        }
    }

    private static void deleteDonDeSang() {
        listDonsDeSang();
        System.out.print("\nID du don à supprimer : ");

        try {
            int id = Integer.parseInt(scanner.nextLine());

            if (donDuSangService.deleteDonDuSang(id)) {
                System.out.println("✅ Don supprimé avec succès !");
            } else {
                System.out.println("❌ Échec de la suppression du don");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un ID valide !");
        }
    }

    // ------------------ GESTION DES RÉPONSES ------------------
    private static void manageReponsesDons() {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Gestion des Réponses ---");
            System.out.println("1. Ajouter une réponse");
            System.out.println("2. Lister toutes les réponses");
            System.out.println("3. Modifier une réponse");
            System.out.println("4. Supprimer une réponse");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> addReponseDon();
                    case 2 -> listReponsesDons();
                    case 3 -> updateReponseDon();
                    case 4 -> deleteReponseDon();
                    case 0 -> back = true;
                    default -> System.out.println("❌ Option invalide !");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Veuillez entrer un nombre valide !");
            }
        }
    }

    private static void addReponseDon() {
        System.out.print("Description de la réponse : ");
        String description = scanner.nextLine();

        // Validate description
        if (description.isEmpty()) {
            System.out.println("❌ La description ne peut pas être vide.");
            return;
        }
        if (description.length() < 10) {
            System.out.println("❌ La description doit contenir au moins 10 caractères.");
            return;
        }
        if (description.length() > 500) {
            System.out.println("❌ La description ne peut pas dépasser 500 caractères.");
            return;
        }

        ReponseDon reponse = new ReponseDon(description);

        try {
            if (reponseDonService.addReponseDon(reponse)) {
                System.out.println("✅ Réponse ajoutée avec succès !");
            } else {
                System.out.println("❌ Échec de l'ajout de la réponse");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de la réponse : " + e.getMessage());
            System.out.println("❌ Échec de l'ajout de la réponse. Vérifiez la configuration de la base de données.");
        }
    }

    private static void listReponsesDons() {
        List<ReponseDon> reponses = reponseDonService.getAllReponseDon();

        if (reponses.isEmpty()) {
            System.out.println("Aucune réponse trouvée.");
        } else {
            System.out.println("\n--- Liste des Réponses ---");
            System.out.printf("%-5s | %-30s%n",
                    "ID", "Description");
            System.out.println("------|--------------------------------");

            for (ReponseDon reponse : reponses) {
                System.out.printf("%-5d | %-30s%n",
                        reponse.getId(),
                        truncate(reponse.getDescription(), 30));
            }
        }
    }

    private static void updateReponseDon() {
        try {
            listReponsesDons();
            System.out.print("\nID de la réponse à modifier : ");

            int id = Integer.parseInt(scanner.nextLine());
            ReponseDon reponse = reponseDonService.getReponseDonById(id);

            if (reponse == null) {
                System.out.println("❌ Aucune réponse trouvée avec cet ID");
                return;
            }

            System.out.print("Nouvelle description (" + reponse.getDescription() + ") : ");
            String description = scanner.nextLine();
            if (!description.isEmpty()) {
                // Validate description
                if (description.length() < 10) {
                    System.out.println("❌ La description doit contenir au moins 10 caractères.");
                    return;
                }
                if (description.length() > 500) {
                    System.out.println("❌ La description ne peut pas dépasser 500 caractères.");
                    return;
                }
                reponse.setDescription(description);
            }

            if (reponseDonService.updateReponseDon(reponse)) {
                System.out.println("✅ Réponse mise à jour avec succès !");
            } else {
                System.out.println("❌ Échec de la mise à jour de la réponse");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un ID valide !");
        }
    }

    private static void deleteReponseDon() {
        try {
            listReponsesDons();
            System.out.print("\nID de la réponse à supprimer : ");

            int id = Integer.parseInt(scanner.nextLine());
            if (reponseDonService.deleteReponseDon(id)) {
                System.out.println("✅ Réponse supprimée avec succès !");
            } else {
                System.out.println("❌ Échec de la suppression de la réponse");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un ID valide !");
        }
    }

    // Méthode utilitaire pour tronquer une chaîne
    private static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}