package com.healthlink.Controllers.User.Authentification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.healthlink.Entites.Role;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
import com.healthlink.Services.GoogleAuthService;
import com.healthlink.Services.UserService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.Node;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import javafx.scene.control.Alert;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Base64;
import java.util.List;
import java.util.Map;

//ajouter par majd
import java.sql.Connection; // Pour la connexion à la base de données
import java.sql.PreparedStatement; // Pour les requêtes préparées
import java.sql.ResultSet; // Pour les résultats des requêtes
import java.sql.SQLException; // Pour gérer les exceptions SQL
import java.sql.Timestamp; // Pour gérer banned_until
import com.healthlink.utils.MyDB; // Import pour MyDB

import javax.imageio.ImageIO;

public class Login {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button resetButton;

    private final AuthService AuthService = new AuthService();

    @FXML
    private Hyperlink forgotPasswordLink;

    private UserService userService;
    public Login() {
        this.userService = new UserService();
        this.googleAuthService = new GoogleAuthService();

    }
    //    @FXML
//    private WebView recaptchaWebView;
//
//    private RecaptchaHandler recaptchaHandler;
    @FXML
    void initialize() {
//        recaptchaHandler = new RecaptchaHandler();
//        recaptchaHandler.initRecaptcha(recaptchaWebView);

        forgotPasswordLink.setOnAction(event -> showForgotPasswordPopup());
        loginButton.setOnAction(this::handleLogin);
        resetButton.setOnAction(event -> {
            emailField.clear();
            passwordField.clear();
        });
    }

    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", "Veuillez remplir tous les champs.");
            return;
        }
        try {
            Utilisateur utilisateur = AuthService.login(email, password);
            if (utilisateur != null) {
                //ajouter par majd
                // Vérifier si l'utilisateur est banni
                UserService userService = new UserService();
                if (userService.isUserBanned(utilisateur.getId())) {
                    // Récupérer la date de fin de bannissement pour l'afficher dans le message
                    String req = "SELECT banned_until FROM utilisateur WHERE id = ?";
                    try (Connection conn = MyDB.getInstance().getConnection();
                         PreparedStatement pst = conn.prepareStatement(req)) {
                        pst.setInt(1, utilisateur.getId());
                        ResultSet rs = pst.executeQuery();
                        if (rs.next()) {
                            Timestamp bannedUntil = rs.getTimestamp("banned_until");
                            showAlert(Alert.AlertType.ERROR, "Compte banni",
                                    "Votre compte est banni jusqu'au " + bannedUntil + ".");
                        }
                    } catch (SQLException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la vérification du bannissement : " + e.getMessage());
                    }
                    return;
                }//
                if (!"approuvé".equalsIgnoreCase(utilisateur.getStatut())) {
                    // Afficher une page ou alerte pour statut non approuvé
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/NonApprouve.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                    return;
                }

                // 1. STOCKER L'UTILISATEUR CONNECTÉ DANS LE SERVICE
                AuthService.setConnectedUtilisateur(utilisateur);

                showAlert(Alert.AlertType.INFORMATION, "Connexion réussie", "Bienvenue " + utilisateur.getPrenom() + " !");

                // 2. REDIRECTION VERS LA PAGE D'ACCUEIL
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();


            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de connexion", "Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite : " + e.getMessage());
        }
    }


    //    private void showAlert(AlertType alertType, String title, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
    @FXML
    private void handleRegisterLink(ActionEvent event) {
        loadView("/views/User/Auth/register.fxml", event);
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
    private void showForgotPasswordPopup() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Récupération du mot de passe");
        dialog.setHeaderText("Entrez votre adresse email");

        ButtonType sendButtonType = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        TextField emailField = new TextField();
        emailField.setPromptText("Adresse email");

        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Email:"), emailField);
        dialog.getDialogPane().setContent(content);

        Button sendButton = (Button) dialog.getDialogPane().lookupButton(sendButtonType);
        sendButton.setDisable(true);

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            sendButton.setDisable(newValue.trim().isEmpty() || !isValidEmail(newValue));
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == sendButtonType) {
                String email = emailField.getText();
                boolean emailExists = userService.checkEmailExists(email);

                if (emailExists) {
                    boolean emailSent = userService.sendResetCode(email);
                    if (emailSent) {
                        showCodeVerificationPopup(email);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText(null);
                        alert.setContentText("Échec de l'envoi du code. Veuillez réessayer.");
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Cet email n'existe pas dans notre base de données.");
                    alert.showAndWait();
                }
            }
        });
    }

    private void showCodeVerificationPopup(String email) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Vérification du code");
        dialog.setHeaderText("Entrez le code de 4 chiffres reçu par email");

        ButtonType verifyButtonType = new ButtonType("Vérifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(verifyButtonType, ButtonType.CANCEL);

        TextField codeField = new TextField();
        codeField.setPromptText("Code de vérification");

        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Code:"), codeField);
        dialog.getDialogPane().setContent(content);

        Button verifyButton = (Button) dialog.getDialogPane().lookupButton(verifyButtonType);
        verifyButton.setDisable(true);

        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            verifyButton.setDisable(newValue.trim().length() != 4 || !newValue.matches("\\d{4}"));
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == verifyButtonType) {
                try {
                    int code = Integer.parseInt(codeField.getText());
                    boolean isCodeValid = userService.verifyResetCode(email, code);
                    if (isCodeValid) {
                        showPasswordResetPopup(email);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText(null);
                        alert.setContentText("Code incorrect. Veuillez réessayer.");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Veuillez entrer un code valide de 4 chiffres.");
                    alert.showAndWait();
                }
            }
        });
    }

    private void showPasswordResetPopup(String email) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau mot de passe");
        dialog.setHeaderText("Entrez votre nouveau mot de passe");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le mot de passe");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nouveau mot de passe:"), passwordField,
                new Label("Confirmer:"), confirmPasswordField
        );
        dialog.getDialogPane().setContent(content);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = newValue.length() >= 8 && newValue.equals(confirmPasswordField.getText());
            saveButton.setDisable(!isValid);
        });
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = newValue.length() >= 8 && newValue.equals(passwordField.getText());
            saveButton.setDisable(!isValid);
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                String newPassword = passwordField.getText();
                boolean passwordUpdated = userService.updatePassword(email, newPassword);
                Alert alert = new Alert(passwordUpdated ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                alert.setTitle(passwordUpdated ? "Succès" : "Erreur");
                alert.setHeaderText(null);
                alert.setContentText(passwordUpdated
                        ? "Votre mot de passe a été mis à jour avec succès."
                        : "Échec de la mise à jour du mot de passe. Veuillez réessayer.");
                alert.showAndWait();
            }
        });
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }
    @FXML
    private Button googleLoginButton;
    private GoogleAuthService googleAuthService;
    private WebView webView;
    private Stage authStage;

    @FXML
    void handleGoogleLogin(ActionEvent event) {
        // Ouvrir une WebView pour la connexion
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
            System.out.println("Navigated to : " + newValue);
            if (newValue.startsWith("http://localhost:8888/Callback")) {
                handleGoogleLoginCallback(newValue); // <--- On appelle la fonction de login ici
            }
        });

        authStage.show();
    }
    private void handleGoogleLoginCallback(String callbackUrl) {
        googleAuthService = new GoogleAuthService();
        try {
            // Extraire le code d'autorisation
            URI uri = new URI(callbackUrl);
            String query = uri.getQuery();
            String code = null;
            for (String param : query.split("&")) {
                if (param.startsWith("code=")) {
                    code = param.substring(5);
                    break;
                }
            }

            if (code == null) {
                throw new RuntimeException("Code d'autorisation manquant !");
            }

            // Récupérer le token d'accès
            GoogleTokenResponse tokenResponse = googleAuthService.getTokenResponse(code);
            String accessToken = tokenResponse.getAccessToken();

            authStage.close();

            // Récupérer les infos de l'utilisateur
            Map<String, Object> userInfo = getGoogleUserInfo(accessToken);
            String email = (String) userInfo.get("email");

            // Vérifier si l'utilisateur existe dans la base
            Utilisateur utilisateur = userService.getUtilisateurByEmail(email);

            if (utilisateur != null) {
                // Utilisateur trouvé => récupérer le rôle
                //Utilisateur utilisateur = userService.getUtilisateurByEmail(email);
                Role role = utilisateur.getRole();
                System.out.println(role.getNom());


                // Stocker en session (optionnel mais utile si tu veux suivre l'utilisateur connecté)
                AuthService.setConnectedUtilisateur(utilisateur);

                Platform.runLater(() -> {
                    showAlert("Succès", "Connexion réussie avec Google pour : " + email + "\nRôle: " + role, Alert.AlertType.INFORMATION);
                    allerALaPageAccueil();
                });
            } else {
                // Utilisateur non trouvé
                Platform.runLater(() -> {
                    showAlert("Erreur", "Aucun compte associé à cet e-mail : " + email, Alert.AlertType.ERROR);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la connexion Google : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean emailExisteDansLaBase(String email) {

        Utilisateur utilisateur = userService.getUtilisateurByEmail(email);
        return utilisateur != null;
    }
    private void allerALaPageAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) googleLoginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
    private Button faceLoginButton;

    private static final String API_URL = "http://localhost:5000/recognize";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private void handleFaceLogin(ActionEvent event) throws IOException {
        System.out.println("Démarrage du processus de connexion par visage");
        try {
            String base64Image = captureWebcamImage();
            if (base64Image == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de capturer une image depuis la webcam.");
                return;
            }

            String jsonBody = String.format("{\"image\": \"%s\"}", base64Image);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la requête API : " + response.statusCode());
                return;
            }

            String responseBody = response.body();
            ObjectNode jsonResponse = mapper.readValue(responseBody, ObjectNode.class);

            if ("success".equals(jsonResponse.get("status").asText())) {
                int userId = jsonResponse.get("user_id").asInt();

                List<Utilisateur> users = new AuthService().getAllUsersWithImageProfile();
                Utilisateur matchedUser = users.stream()
                        .filter(user -> user.getId() == userId)
                        .findFirst()
                        .orElse(null);

                if (matchedUser == null) {
                    showAlert(Alert.AlertType.ERROR, "Échec", "Utilisateur non trouvé.");
                    return;
                }

                if (!"approuvé".equalsIgnoreCase(matchedUser.getStatut())) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/NonApprouve.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) faceLoginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                    return;
                }

                AuthService.setConnectedUtilisateur(matchedUser);
                showAlert(Alert.AlertType.INFORMATION, "Connexion réussie", "Bienvenue " + matchedUser.getPrenom() + " !");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) faceLoginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec", jsonResponse.get("message").asText());
            }
        } catch (Exception e) {
            System.err.println("Erreur dans la connexion par visage : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    private String captureWebcamImage() {
        try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
            grabber.start();
            Frame frame = grabber.grab();
            if (frame == null) {
                return null;
            }

            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bufferedImage = converter.convert(frame);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
