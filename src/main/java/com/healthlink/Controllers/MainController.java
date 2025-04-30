package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Entities.ForumResponse;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
import com.healthlink.Services.ForumService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    // Remplacez la valeur statique par une récupération dynamique
    private int currentUserId;
    private Forum currentForum;

    // Ajoutez une instance de AuthService
    private final AuthService authService = new AuthService();

    @FXML
    private StackPane mainContainer;

    @FXML
    public void initialize() {
        // Récupérer l'ID de l'utilisateur connecté au démarrage
        Utilisateur connectedUser = authService.getConnectedUtilisateur();
        if (connectedUser != null) {
            this.currentUserId = connectedUser.getId();
            System.out.println("Utilisateur connecté récupéré - ID: " + currentUserId);
        } else {
            System.err.println("Aucun utilisateur connecté trouvé dans AuthService");
            this.currentUserId = -1; // Valeur par défaut si aucun utilisateur n'est connecté
        }

        showForumList(); // Charge la vue par défaut au démarrage
        setupKeyboardShortcut();
    }

    private void setupKeyboardShortcut() {
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.A) {
            showAdminDashboard();
            event.consume();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContainer.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showForumList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ForumList.fxml"));
            Parent view = loader.load();

            ForumListController controller = loader.getController();
            controller.setMainController(this);

            mainContainer.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showCreateForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CreateForum.fxml"));
            Parent view = loader.load();

            CreateForumController controller = loader.getController();
            controller.setMainController(this);
            controller.setCurrentUserId(currentUserId); // Passer currentUserId

            mainContainer.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showForumDetails(Forum forum) {
        this.currentForum = forum;
        try {
            System.out.println("Affichage des détails du forum - forumId: " + forum.getId() + ", userId: " + currentUserId);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ShowForum.fxml"));
            Parent view = loader.load();

            ShowForumController controller = loader.getController();
            controller.setForum(forum, currentUserId);
            controller.setMainController(this);

            mainContainer.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showEditForum(Forum forum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EditForum.fxml"));
            Parent view = loader.load();

            EditForumController controller = loader.getController();
            controller.setForum(forum, currentUserId);
            controller.setMainController(this);

            mainContainer.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Forum getCurrentForum() {
        return currentForum;
    }

    public void showEditComment(ForumResponse response) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EditComment.fxml"));
            Parent view = loader.load();

            EditCommentController controller = loader.getController();
            controller.setResponse(response);
            controller.setMainController(this);

            mainContainer.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AdminDashboard.fxml"));
            Parent view = loader.load();

            mainContainer.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dashboard admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void showGenerateForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/generateForum.fxml"));
            Parent root = loader.load();

            GenerateForumController controller = loader.getController();
            controller.setMainController(this);
            controller.setCurrentUserId(currentUserId);

            mainContainer.getChildren().setAll(root); // Utilisez mainContainer au lieu de mainContent

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface de génération de forum: " + e.getMessage());
        }
    }
    public void createForum(Forum forum) {
        ForumService forumService = new ForumService();
        forumService.add(forum);
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}