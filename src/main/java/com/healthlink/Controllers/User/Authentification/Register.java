package com.healthlink.Controllers.User.Authentification;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.healthlink.Entites.Role;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
//import com.healthlink.Services.UserService;
import com.healthlink.Services.GoogleAuthService;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.ResourceBundle;

public class Register {

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
        // Vérification avec QuickEmailVerification
        if (!com.healthlink.Services.EmailVerifier.verifierEmail(emailTextField.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'email");
            alert.setHeaderText(null);
            alert.setContentText("L'adresse email est invalide, temporaire ou n'existe pas !");
            alert.show();
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

            AuthService AuthService = new AuthService();
            AuthService.addPatient(utilisateur);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Patient ajouté avec succès !");
            alert.show();
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
        // Vérification avec QuickEmailVerification
        if (!com.healthlink.Services.EmailVerifier.verifierEmail(emailTextField.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'email");
            alert.setHeaderText(null);
            alert.setContentText("L'adresse email est invalide, temporaire ou n'existe pas !");
            alert.show();
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

            AuthService authService = new AuthService();
            authService.addMedecin(utilisateur);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Médecin ajouté avec succès !");
            alert.show();
            // Redirection vers la page de confirmation
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/ConfirmationInscription.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            // Optionnel: vider les champs après l'ajout
//            nomTextField.clear();
//            prenomTextField.clear();
//            emailTextField.clear();
//            motdepasseTextField.clear();
//            numtelTextField.clear();
//            adresseTextField.clear();
//            diplomeTextField.clear();
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
        // Vérification avec QuickEmailVerification
        if (!com.healthlink.Services.EmailVerifier.verifierEmail(emailTextField.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'email");
            alert.setHeaderText(null);
            alert.setContentText("L'adresse email est invalide, temporaire ou n'existe pas !");
            alert.show();
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
            AuthService authService = new AuthService();
            authService.addSoignant(utilisateur);

            // Affichage du message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Soignant ajouté avec succès !");
            alert.showAndWait();
            // Redirection vers la page de confirmation
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/ConfirmationInscription.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

//            // Réinitialisation des champs
//            nomTextField.clear();
//            prenomTextField.clear();
//            emailTextField.clear();
//            motdepasseTextField.clear();
//            categorieSoinTextField.clear();
//            //diplomeTextField.clear();
//            imageTextField.clear();

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
    @FXML
    private void handleRegister(ActionEvent event) {
        loadView("/views/User/Auth/register.fxml", event);
    }
    @FXML
    private void handleRegisterM(ActionEvent event) {
        loadView("/views/User/Auth/registerM.fxml", event);
    }
    @FXML
    private void handleRegisterS(ActionEvent event) {
        loadView("/views/User/Auth/registerS.fxml", event);
    }
    @FXML
    private void handleLogin(ActionEvent event) {
        loadView("/views/User/Auth/login.fxml", event);
    }

    // Méthode générique pour charger les vues
    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            // 1. Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 2. Obtenir la stage actuelle à partir de l'événement
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            // 3. Changer la scène
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue: " + fxmlPath);

            // Optionnel: Afficher une alerte à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger la vue");
            alert.setContentText("Le fichier " + fxmlPath + " est introuvable ou corrompu.");
            alert.showAndWait();
        }
    }

    private GoogleAuthService googleAuthService;
    private WebView webView;
    private Stage authStage;

    public Register() {
        this.googleAuthService = new GoogleAuthService();
    }

    @FXML
    void handleGoogleSignIn(ActionEvent event) {
        // Créer une nouvelle fenêtre pour l'authentification
        authStage = new Stage();
        StackPane root = new StackPane();
        webView = new WebView();
        root.getChildren().add(webView);

        Scene scene = new Scene(root, 600, 800);
        authStage.setScene(scene);
        authStage.setTitle("Connexion avec Google");

        // Charger l'URL d'authentification
        String authUrl = googleAuthService.getAuthorizationUrl();
        webView.getEngine().load(authUrl);

        // Écouter les changements d'URL pour capturer le code d'autorisation
        webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.startsWith("http://localhost:8888/Callback")) {
                handleGoogleCallback(newValue);
            }
        });

        authStage.show();
    }


    private Map<String, Object> getGoogleUserInfo(String accessToken) throws IOException {
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        String url = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken;
        HttpRequest request = transport.createRequestFactory()
                .buildGetRequest(new GenericUrl(url));

        HttpResponse response = request.execute();
        String jsonResponse = response.parseAsString();

        // Parse la réponse JSON en Map
        return jsonFactory.fromString(jsonResponse, Map.class);
    }

    @FXML
    private void handleGoogleCallback(String callbackUrl) {
        try {
            // Extraction plus robuste du code
            URI uri = new URI(callbackUrl);
            String query = uri.getQuery();
            if (query == null) {
                throw new RuntimeException("No query parameters in callback URL");
            }

            String[] params = query.split("&");
            String code = null;
            for (String param : params) {
                if (param.startsWith("code=")) {
                    code = param.substring(5);
                    break;
                }
            }

            if (code == null || code.isEmpty()) {
                throw new RuntimeException("No authorization code in callback URL");
            }

            // Debug: Afficher le code reçu
            System.out.println("Code reçu: " + code);

            GoogleTokenResponse tokenResponse = googleAuthService.getTokenResponse(code);
            String accessToken = tokenResponse.getAccessToken();

            authStage.close();

            // Récupérer les infos utilisateur
            Map<String, Object> userInfo = getGoogleUserInfo(accessToken);

            // Remplir le formulaire...
            Platform.runLater(() -> {
                nomTextField.setText((String) userInfo.get("family_name"));
                prenomTextField.setText((String) userInfo.get("given_name"));
                emailTextField.setText((String) userInfo.get("email"));

                String randomPassword = generateRandomPassword();
                motdepasseTextField.setText(randomPassword);
//
//                String pictureUrl = (String) userInfo.get("picture");
//                if (pictureUrl != null) {
//                    saveProfileImageFromUrl(pictureUrl);
//                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'authentification Google: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

//    private void saveProfileImageFromUrl(String imageUrl) {
//        try {
//            URL url = new URL(imageUrl);
//            BufferedImage image = ImageIO.read(url);
//            File outputFile = new File("profile_images/" + emailTextField.getText() + ".jpg");
//            ImageIO.write(image, "jpg", outputFile);
//        } catch (IOException e) {
//            System.err.println("Erreur lors de la sauvegarde de l'image: " + e.getMessage());
//        }
//}
    private String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Au moins un caractère de chaque type
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // 4 caractères aléatoires supplémentaires
        for (int i = 0; i < 4; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // Mélanger le mot de passe
        char[] array = password.toString().toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }

        return new String(array);
    }
}
