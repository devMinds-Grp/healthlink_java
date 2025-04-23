package com.healthlink.Controllers.User;

import com.healthlink.Entites.Role;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Ajout {

    @FXML
    private TextField nomTextField;

    @FXML
    private TextField prenomTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField motdepasseTextField;

    @FXML
    private TextField numtelTextField;

    @FXML
    private TextField diplomeTextField;

    @FXML
    private TextField adresseTextField;

    @FXML
    private ComboBox<String> specialiteComboBox;

    @FXML
    private TextField specialiteTextField;

    @FXML
    private TextField imageTextField;

    @FXML
    private Label nomFichierLabel;
    private File fichierImage;
    @FXML
    private Button choisirImageButton;
    @FXML
    private Button choisirDiplomeButton;
    @FXML
    private Label nomDiplomeLabel;
    private File fichierDiplome;

    @FXML
    private void handleChoisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de profil");

        // Filtres pour les types de fichiers images
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        Stage stage = (Stage) choisirImageButton.getScene().getWindow();
        fichierImage = fileChooser.showOpenDialog(stage);

        if (fichierImage != null) {
            nomFichierLabel.setText(fichierImage.getName());
        } else {
            nomFichierLabel.setText("Aucun fichier choisi");
        }
    }

    @FXML
    void addPatient(ActionEvent event) {
        // Validation des champs obligatoires
        if (!validateRequiredField(nomTextField.getText(), "nom") ||
                !validateRequiredField(prenomTextField.getText(), "prénom") ||
                !validateEmail(emailTextField.getText()) ||
                !validatePassword(motdepasseTextField.getText()) ||
                !validatePhone(numtelTextField.getText())) {
            return;
        }
        try {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(nomTextField.getText());
            utilisateur.setPrenom(prenomTextField.getText());
            utilisateur.setEmail(emailTextField.getText());
            utilisateur.setMot_de_passe(motdepasseTextField.getText());
            utilisateur.setNum_tel(Integer.parseInt(numtelTextField.getText()));

            // Gestion de l'image de profil
            if (fichierImage != null) {
                // Sauvegarder le chemin de l'image
                utilisateur.setImageprofile(fichierImage.getAbsolutePath());

                // Optionnel: Copier l'image dans un dossier spécifique
                String destinationPath = saveProfileImage(fichierImage);
                utilisateur.setImageprofile(destinationPath);
            }
            // Affecter le rôle "Patient"
            Role rolePatient = new Role();
            rolePatient.setId(3);
            utilisateur.setRole(rolePatient);

            UserService userService = new UserService();
            userService.addPatient(utilisateur);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Patient ajouté avec succès !");
            alert.show();
            nomTextField.clear();
            prenomTextField.clear();
            emailTextField.clear();
            motdepasseTextField.clear();
            numtelTextField.clear();

            // Réinitialiser les labels des fichiers
            nomFichierLabel.setText("Aucun fichier choisi");

            // Réinitialiser les fichiers sélectionnés
            fichierImage = null;
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ajout du patient.");
            alert.show();
        }
    }

    // Ajoutez cette méthode pour sauvegarder l'image
    private String saveProfileImage(File imageFile) {
        try {
            // Créer un dossier images s'il n'existe pas
            File dossierImages = new File("profile_images");
            if (!dossierImages.exists()) {
                dossierImages.mkdir();
            }

            // Générer un nom unique pour le fichier
            String nomFichier = System.currentTimeMillis() + "_" + imageFile.getName();
            File destination = new File(dossierImages, nomFichier);

            // Copier le fichier
            java.nio.file.Files.copy(
                    imageFile.toPath(),
                    destination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return nomFichier;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @FXML
    void addMedecin(ActionEvent event) {
        // Validation des champs obligatoires
        if (!validateRequiredField(nomTextField.getText(), "nom") ||
                !validateRequiredField(prenomTextField.getText(), "prénom") ||
                !validateEmail(emailTextField.getText()) ||
                !validatePassword(motdepasseTextField.getText()) ||
                !validatePhone(numtelTextField.getText()) ||
                !validateRequiredField(adresseTextField.getText(), "adresse") ||
                !validateRequiredField(specialiteTextField.getText(), "spécialité") ||
                !validateDiplome(fichierDiplome) ||
                !validateImage(fichierImage)) {
            return;
        }
        try {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(nomTextField.getText());
            utilisateur.setPrenom(prenomTextField.getText());
            utilisateur.setEmail(emailTextField.getText());
            utilisateur.setMot_de_passe(motdepasseTextField.getText());
            utilisateur.setNum_tel(Integer.parseInt(numtelTextField.getText()));
            utilisateur.setAdresse(adresseTextField.getText());
            utilisateur.setSpeciality(specialiteTextField.getText()); // Utilisation du champ diplôme comme spécialité

            // Gestion de l'image de profil
            if (fichierImage != null) {
                // Sauvegarder le chemin de l'image
                utilisateur.setImageprofile(fichierImage.getAbsolutePath());

                // Optionnel: Copier l'image dans un dossier spécifique
                String destinationPath = saveProfileImage(fichierImage);
                utilisateur.setImageprofile(destinationPath);
            }
            if (fichierDiplome != null) {
                String diplomePath = saveDiplome(fichierDiplome);
                utilisateur.setImage(diplomePath); // Utilisez la variable diplomePath qui vient d'être créée
            }
            // Affecter le rôle "Médecin"
            Role roleMedecin = new Role();
            roleMedecin.setId(2); // ID 2 pour médecin
            utilisateur.setRole(roleMedecin);

            UserService userService = new UserService();
            userService.addMedecin(utilisateur);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Médecin ajouté avec succès !");
            alert.show();

             //Optionnel: vider les champs après l'ajout
            nomTextField.clear();
            prenomTextField.clear();
            emailTextField.clear();
            motdepasseTextField.clear();
            numtelTextField.clear();
            adresseTextField.clear();
            specialiteTextField.clear();
            // Réinitialiser les labels des fichiers
            nomDiplomeLabel.setText("Aucun fichier choisi");
            nomFichierLabel.setText("Aucun fichier choisi");

            // Réinitialiser les fichiers sélectionnés
            fichierDiplome = null;
            fichierImage = null;
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ajout du médecin: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    void addSoignant(ActionEvent event) {
        // Validation des champs obligatoires
        if (!validateRequiredField(nomTextField.getText(), "nom") ||
                !validateRequiredField(prenomTextField.getText(), "prénom") ||
                !validateEmail(emailTextField.getText()) ||
                !validatePassword(motdepasseTextField.getText()) ||
                !validateRequiredField(specialiteComboBox.getValue(), "catégorie de soin") ||
                !validateDiplome(fichierDiplome) ||
                !validateImage(fichierImage)) {
            return;
        }
        try {
            // Création de l'objet User avec les données du formulaire
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(nomTextField.getText());
            utilisateur.setPrenom(prenomTextField.getText());
            utilisateur.setEmail(emailTextField.getText());
            utilisateur.setMot_de_passe(motdepasseTextField.getText());
            utilisateur.setCategorie_soin(specialiteComboBox.getValue());


            // Gestion de l'image de profil
            if (fichierImage != null) {
                // Sauvegarder le chemin de l'image
                utilisateur.setImageprofile(fichierImage.getAbsolutePath());

                // Optionnel: Copier l'image dans un dossier spécifique
                String destinationPath = saveProfileImage(fichierImage);
                utilisateur.setImageprofile(destinationPath);
            }

            // Gestion du diplôme
//            if (fichierDiplome != null) {
//                user.setImage(fichierDiplome.getAbsolutePath());
//                String diplomePath = saveDiplome(fichierDiplome);
//                // Stockez le chemin dans speciality ou un autre champ approprié
//                user.setImage(destinationPath);
//            }
            if (fichierDiplome != null) {
                String diplomePath = saveDiplome(fichierDiplome);
                utilisateur.setImage(diplomePath); // Utilisez la variable diplomePath qui vient d'être créée
            }


            // Définition du rôle Soignant (ID 4)
            Role roleSoignant = new Role();
            roleSoignant.setId(4);
            utilisateur.setRole(roleSoignant);

            // Appel du service pour ajouter le soignant
            UserService userService = new UserService();
            userService.addSoignant(utilisateur);

            // Affichage du message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Soignant ajouté avec succès !");
            alert.showAndWait();

            // Réinitialisation des champs
            nomTextField.clear();
            prenomTextField.clear();
            emailTextField.clear();
            motdepasseTextField.clear();
            // Réinitialiser les labels des fichiers
            nomDiplomeLabel.setText("Aucun fichier choisi");
            nomFichierLabel.setText("Aucun fichier choisi");

            // Réinitialiser les fichiers sélectionnés
            fichierDiplome = null;
            fichierImage = null;

        } catch (Exception e) {
            // Gestion des erreurs
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ajout du soignant: " + e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML
    private void handleChoisirDiplome() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier de diplôme");

        // Filtres pour les types de fichiers (PDF, images, etc.)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        Stage stage = (Stage) choisirDiplomeButton.getScene().getWindow();
        fichierDiplome = fileChooser.showOpenDialog(stage);

        if (fichierDiplome != null) {
            nomDiplomeLabel.setText(fichierDiplome.getName());
        } else {
            nomDiplomeLabel.setText("Aucun fichier choisi");
        }
    }

    private String saveDiplome(File diplomeFile) {
        try {
            // Créer un dossier diplomes s'il n'existe pas
            File dossierDiplomes = new File("diplomes");
            if (!dossierDiplomes.exists()) {
                dossierDiplomes.mkdir();
            }

            // Générer un nom unique pour le fichier
            String nomFichier = System.currentTimeMillis() + "_" + diplomeFile.getName();
            File destination = new File(dossierDiplomes, nomFichier);

            // Copier le fichier
            java.nio.file.Files.copy(
                    diplomeFile.toPath(),
                    destination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return nomFichier;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private boolean validateRequiredField(String field, String fieldName) {
        if (field == null || field.trim().isEmpty()) {
            showAlert("Erreur de saisie", "Le champ " + fieldName + " est obligatoire");
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (email == null || !email.matches(emailRegex)) {
            showAlert("Erreur de saisie", "Email invalide");
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        // Au moins 8 caractères, une majuscule, une minuscule, un chiffre
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (password == null || !password.matches(passwordRegex)) {
            showAlert("Erreur de saisie", "Le mot de passe doit contenir:\n- Au moins 8 caractères\n- Une majuscule\n- Une minuscule\n- Un chiffre");
            return false;
        }
        return true;
    }

    private boolean validatePhone(String phone) {
        try {
            if (phone == null || phone.length() != 8) {
                showAlert("Erreur de saisie", "Le numéro de téléphone doit contenir 8 chiffres");
                return false;
            }
            Integer.parseInt(phone);
            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur de saisie", "Le numéro de téléphone ne doit contenir que des chiffres");
            return false;
        }
    }

    private boolean validateImage(File imageFile) {
        if (imageFile == null) {
            showAlert("Erreur de saisie", "Une image de profil est requise");
            return false;
        }
        return true;
    }

    private boolean validateDiplome(File diplomeFile) {
        if (diplomeFile == null) {
            showAlert("Erreur de saisie", "Un fichier de diplôme est requis");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Ajoutez cette annotation au-dessus de votre contrôleur
    @FXML
    private Button submitButton;

}

