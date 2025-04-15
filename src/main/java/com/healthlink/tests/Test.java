package com.healthlink.tests;

import com.healthlink.Entites.User;
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

        User newUser = new User();

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
        newUser.setRole(selectedRole);

        System.out.print("Nom: ");
        newUser.setNom(scanner.nextLine());

        System.out.print("Prénom: ");
        newUser.setPrenom(scanner.nextLine());

        System.out.print("Email: ");
        newUser.setEmail(scanner.nextLine());

        System.out.print("Mot de passe: ");
        newUser.setMot_de_passe(scanner.nextLine());

        System.out.print("Numéro de téléphone: ");
        newUser.setNum_tel(scanner.nextInt());
        scanner.nextLine();

        System.out.print("Adresse: ");
        newUser.setAdresse(scanner.nextLine());

        System.out.print("Spécialité: ");
        newUser.setSpeciality(scanner.nextLine());

        System.out.print("Catégorie de soin: ");
        newUser.setCategorie_soin(scanner.nextLine());

        System.out.print("Image ID: ");
        newUser.setImage(scanner.nextLine());

        System.out.print("Image Profile ID: ");
        newUser.setImageprofile(scanner.nextLine());
        scanner.nextLine();

        System.out.print("Statut: ");
        newUser.setStatut(scanner.nextLine());

        System.out.print("Reset Code: ");
        newUser.setReset_code(scanner.nextInt());
        scanner.nextLine();

        userService.add(newUser);
        System.out.println("Utilisateur ajouté avec succès!");
    }

    private static void updateUser() {
        System.out.println("\nModification d'un utilisateur:");
        displayUsers();

        System.out.print("ID de l'utilisateur à modifier: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        // Récupérer l'utilisateur existant
        User existingUser = userService.findById(id);
        if (existingUser == null) {
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
            existingUser.setRole(newRole);
        }

        System.out.print("Nouveau nom (" + existingUser.getNom() + ") : ");
        String nom = scanner.nextLine();
        if (!nom.isEmpty()) {
            existingUser.setNom(nom);
        }

        System.out.print("Nouveau prénom (" + existingUser.getPrenom() + ") : ");
        String prenom = scanner.nextLine();
        if (!prenom.isEmpty()) {
            existingUser.setPrenom(prenom);
        }

        System.out.print("Nouvel email (" + existingUser.getEmail() + ") : ");
        String email = scanner.nextLine();
        if (!email.isEmpty()) {
            existingUser.setEmail(email);
        }

        System.out.print("Nouveau mot de passe: ");
        String password = scanner.nextLine();
        if (!password.isEmpty()) {
            existingUser.setMot_de_passe(password);
        }

        System.out.print("Nouveau numéro de téléphone (" + existingUser.getNum_tel() + ") : ");
        String telInput = scanner.nextLine();
        if (!telInput.isEmpty()) {
            existingUser.setNum_tel(Integer.parseInt(telInput));
        }

        System.out.print("Nouvelle adresse (" + existingUser.getAdresse() + ") : ");
        String adresse = scanner.nextLine();
        if (!adresse.isEmpty()) {
            existingUser.setAdresse(adresse);
        }

        System.out.print("Nouvelle spécialité (" + existingUser.getSpeciality() + ") : ");
        String speciality = scanner.nextLine();
        if (!speciality.isEmpty()) {
            existingUser.setSpeciality(speciality);
        }

        System.out.print("Nouvelle catégorie de soin (" + existingUser.getCategorie_soin() + ") : ");
        String categorieSoin = scanner.nextLine();
        if (!categorieSoin.isEmpty()) {
            existingUser.setCategorie_soin(categorieSoin);
        }

        System.out.print("Nouvel Image ID (" + existingUser.getImage() + ") : ");
        String imageInput = scanner.nextLine();
        if (!imageInput.isEmpty()) {
            existingUser.setImage(imageInput);
        }

        System.out.print("Nouvel Image Profile ID (" + existingUser.getImageprofile() + ") : ");
        String imageProfileInput = scanner.nextLine();
        if (!imageProfileInput.isEmpty()) {
            existingUser.setImageprofile(imageProfileInput);
        }

        System.out.print("Nouveau statut (" + existingUser.getStatut() + ") : ");
        String statut = scanner.nextLine();
        if (!statut.isEmpty()) {
            existingUser.setStatut(statut);
        }

        System.out.print("Nouveau reset code (" + existingUser.getReset_code() + ") : ");
        String resetCodeInput = scanner.nextLine();
        if (!resetCodeInput.isEmpty()) {
            existingUser.setReset_code(Integer.parseInt(resetCodeInput));
        }

        userService.update(existingUser);
        System.out.println("Utilisateur modifié avec succès!");
    }

    private static void deleteUser() {
        System.out.println("\nSuppression d'un utilisateur:");
        displayUsers();

        System.out.print("ID de l'utilisateur à supprimer: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        User userToDelete = userService.findById(id);
        if (userToDelete == null) {
            System.out.println("Utilisateur non trouvé!");
            return;
        }

        userService.delete(userToDelete);
        System.out.println("Utilisateur supprimé avec succès!");
    }

    private static void displayUsers() {
        System.out.println("\n=== LISTE DES UTILISATEURS ===");
        List<User> users = userService.find();

        if (users.isEmpty()) {
            System.out.println("Aucun utilisateur trouvé.");
        } else {
            // En-tête du tableau
            System.out.println("+----+---------------------+---------------------+---------------------+---------------------+---------------------+");
            System.out.println("| ID |        Nom          |       Prénom        |        Email        |        Rôle         |      Spécialité     |");
            System.out.println("+----+---------------------+---------------------+---------------------+---------------------+---------------------+");

            for (User user : users) {
                String roleName = (user.getRole() != null) ? user.getRole().getNom() : "Non défini";
                String speciality = (user.getSpeciality() != null) ? user.getSpeciality() : "N/A";

                System.out.printf("| %-2d | %-19s | %-19s | %-19s | %-19s | %-19s |%n",
                        user.getId(),
                        truncate(user.getNom(), 19),
                        truncate(user.getPrenom(), 19),
                        truncate(user.getEmail(), 19),
                        truncate(roleName, 19),
                        truncate(speciality, 19));
            }

            System.out.println("+----+---------------------+---------------------+---------------------+---------------------+---------------------+");
            System.out.println("Total: " + users.size() + " utilisateur(s)");
        }
    }

    private static String truncate(String str, int length) {
        if (str == null) {
            return "";
        }
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}