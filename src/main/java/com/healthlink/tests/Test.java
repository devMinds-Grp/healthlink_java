package com.healthlink.tests;

<<<<<<< HEAD

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Test extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/add_category.fxml"));
        primaryStage.setTitle("Formini application");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
=======
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entites.Role;
import com.healthlink.Services.UserService;
import com.healthlink.Services.RoleService;
import java.util.List;
import java.util.Scanner;

public class Test {
    private static Scanner scanner = new Scanner(System.in);
    private static UserService userService = new UserService();
    private static RoleService roleService = new RoleService();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\nMenu Principal:");
            System.out.println("1. Ajouter un utilisateur");
            System.out.println("2. Modifier un utilisateur");
            System.out.println("3. Supprimer un utilisateur");
            System.out.println("4. Afficher tous les utilisateurs");
            System.out.println("5. Quitter");
            System.out.print("Votre choix: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consommer la nouvelle ligne

            switch (choice) {
                case 1:
                    addUser();
                    break;
                case 2:
                    updateUser();
                    break;
                case 3:
                    deleteUser();
                    break;
                case 4:
                    displayUsers();
                    break;
                case 5:
                    running = false;
                    System.out.println("Au revoir!");
                    break;
                default:
                    System.out.println("Choix invalide!");
            }
        }
    }

    private static void addUser() {
        System.out.println("\nAjout d'un nouvel utilisateur:");

        Utilisateur newUtilisateur = new Utilisateur();

        // Afficher les rôles disponibles et demander à choisir
        List<Role> roles = roleService.find();

        System.out.println("Rôles disponibles:");
        for (Role role : roles) {
            System.out.println(role.getId() + " - " + role.getNom());
        }

        System.out.print("Choisissez l'ID du rôle: ");
        int roleId = scanner.nextInt();
        scanner.nextLine();

        Role selectedRole = new Role();
        selectedRole.setId(roleId);
        newUtilisateur.setRole(selectedRole);

        System.out.print("Nom: ");
        newUtilisateur.setNom(scanner.nextLine());

        System.out.print("Prénom: ");
        newUtilisateur.setPrenom(scanner.nextLine());

        System.out.print("Email: ");
        newUtilisateur.setEmail(scanner.nextLine());

        System.out.print("Mot de passe: ");
        newUtilisateur.setMot_de_passe(scanner.nextLine());

        System.out.print("Numéro de téléphone: ");
        newUtilisateur.setNum_tel(scanner.nextInt());
        scanner.nextLine();

        System.out.print("Adresse: ");
        newUtilisateur.setAdresse(scanner.nextLine());

        System.out.print("Spécialité: ");
        newUtilisateur.setSpeciality(scanner.nextLine());

        System.out.print("Catégorie de soin: ");
        newUtilisateur.setCategorie_soin(scanner.nextLine());

        System.out.print("Image ID: ");
        newUtilisateur.setImage(scanner.nextLine());

        System.out.print("Image Profile ID: ");
        newUtilisateur.setImageprofile(scanner.nextLine());
        scanner.nextLine();

        System.out.print("Statut: ");
        newUtilisateur.setStatut(scanner.nextLine());

        System.out.print("Reset Code: ");
        newUtilisateur.setReset_code(scanner.nextInt());
        scanner.nextLine();

        userService.add(newUtilisateur);
        System.out.println("Utilisateur ajouté avec succès!");
    }

    private static void updateUser() {
        System.out.println("\nModification d'un utilisateur:");
        displayUsers();

        System.out.print("ID de l'utilisateur à modifier: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        // Récupérer l'utilisateur existant
        Utilisateur existingUtilisateur = userService.findById(id);
        if (existingUtilisateur == null) {
            System.out.println("Utilisateur non trouvé!");
            return;
        }

        // Afficher les rôles disponibles
        List<Role> roles = roleService.find();
        System.out.println("Rôles disponibles:");
        for (Role role : roles) {
            System.out.println(role.getId() + " - " + role.getNom());
        }
        System.out.print("Nouveau rôle ID (laissez vide pour ne pas modifier): ");
        String roleInput = scanner.nextLine();
        if (!roleInput.isEmpty()) {
            Role newRole = new Role();
            newRole.setId(Integer.parseInt(roleInput));
            existingUtilisateur.setRole(newRole);
        }

        System.out.print("Nouveau nom (" + existingUtilisateur.getNom() + ") : ");
        String nom = scanner.nextLine();
        if (!nom.isEmpty()) {
            existingUtilisateur.setNom(nom);
        }

        System.out.print("Nouveau prénom (" + existingUtilisateur.getPrenom() + ") : ");
        String prenom = scanner.nextLine();
        if (!prenom.isEmpty()) {
            existingUtilisateur.setPrenom(prenom);
        }

        System.out.print("Nouvel email (" + existingUtilisateur.getEmail() + ") : ");
        String email = scanner.nextLine();
        if (!email.isEmpty()) {
            existingUtilisateur.setEmail(email);
        }

        System.out.print("Nouveau mot de passe: ");
        String password = scanner.nextLine();
        if (!password.isEmpty()) {
            existingUtilisateur.setMot_de_passe(password);
        }

        System.out.print("Nouveau numéro de téléphone (" + existingUtilisateur.getNum_tel() + ") : ");
        String telInput = scanner.nextLine();
        if (!telInput.isEmpty()) {
            existingUtilisateur.setNum_tel(Integer.parseInt(telInput));
        }

        System.out.print("Nouvelle adresse (" + existingUtilisateur.getAdresse() + ") : ");
        String adresse = scanner.nextLine();
        if (!adresse.isEmpty()) {
            existingUtilisateur.setAdresse(adresse);
        }

        System.out.print("Nouvelle spécialité (" + existingUtilisateur.getSpeciality() + ") : ");
        String speciality = scanner.nextLine();
        if (!speciality.isEmpty()) {
            existingUtilisateur.setSpeciality(speciality);
        }

        System.out.print("Nouvelle catégorie de soin (" + existingUtilisateur.getCategorie_soin() + ") : ");
        String categorieSoin = scanner.nextLine();
        if (!categorieSoin.isEmpty()) {
            existingUtilisateur.setCategorie_soin(categorieSoin);
        }

        System.out.print("Nouvel Image ID (" + existingUtilisateur.getImage() + ") : ");
        String imageInput = scanner.nextLine();
        if (!imageInput.isEmpty()) {
            existingUtilisateur.setImage(imageInput);
        }

        System.out.print("Nouvel Image Profile ID (" + existingUtilisateur.getImageprofile() + ") : ");
        String imageProfileInput = scanner.nextLine();
        if (!imageProfileInput.isEmpty()) {
            existingUtilisateur.setImageprofile(imageProfileInput);
        }

        System.out.print("Nouveau statut (" + existingUtilisateur.getStatut() + ") : ");
        String statut = scanner.nextLine();
        if (!statut.isEmpty()) {
            existingUtilisateur.setStatut(statut);
        }

        System.out.print("Nouveau reset code (" + existingUtilisateur.getReset_code() + ") : ");
        String resetCodeInput = scanner.nextLine();
        if (!resetCodeInput.isEmpty()) {
            existingUtilisateur.setReset_code(Integer.parseInt(resetCodeInput));
        }

        userService.update(existingUtilisateur);
        System.out.println("Utilisateur modifié avec succès!");
    }

    private static void deleteUser() {
        System.out.println("\nSuppression d'un utilisateur:");
        displayUsers();

        System.out.print("ID de l'utilisateur à supprimer: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Utilisateur utilisateurToDelete = userService.findById(id);
        if (utilisateurToDelete == null) {
            System.out.println("Utilisateur non trouvé!");
            return;
        }

        userService.delete(utilisateurToDelete);
        System.out.println("Utilisateur supprimé avec succès!");
    }

    private static void displayUsers() {
        System.out.println("\n=== LISTE DES UTILISATEURS ===");
        List<Utilisateur> utilisateurs = userService.find();

        if (utilisateurs.isEmpty()) {
            System.out.println("Aucun utilisateur trouvé.");
        } else {
            // En-tête du tableau
            System.out.println("+----+---------------------+---------------------+---------------------+---------------------+---------------------+");
            System.out.println("| ID |        Nom          |       Prénom        |        Email        |        Rôle         |      Spécialité     |");
            System.out.println("+----+---------------------+---------------------+---------------------+---------------------+---------------------+");

            for (Utilisateur utilisateur : utilisateurs) {
                String roleName = (utilisateur.getRole() != null) ? utilisateur.getRole().getNom() : "Non défini";
                String speciality = (utilisateur.getSpeciality() != null) ? utilisateur.getSpeciality() : "N/A";

                System.out.printf("| %-2d | %-19s | %-19s | %-19s | %-19s | %-19s |%n",
                        utilisateur.getId(),
                        truncate(utilisateur.getNom(), 19),
                        truncate(utilisateur.getPrenom(), 19),
                        truncate(utilisateur.getEmail(), 19),
                        truncate(roleName, 19),
                        truncate(speciality, 19));
            }

            System.out.println("+----+---------------------+---------------------+---------------------+---------------------+---------------------+");
            System.out.println("Total: " + utilisateurs.size() + " utilisateur(s)");
        }
    }

    private static String truncate(String str, int length) {
        if (str == null) {
            return "";
        }
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
>>>>>>> master
    }
}