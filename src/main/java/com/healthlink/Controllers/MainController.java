package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Entities.ForumResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    private int currentUserId = 1;
    private Forum currentForum;

    @FXML
    private StackPane mainContainer;

    @FXML
    public void initialize() {
        showForumList(); // Charge la vue par défaut au démarrage

        // Ajout du gestionnaire d'événements clavier
        setupKeyboardShortcut();
    }

    private void setupKeyboardShortcut() {
        // Attendre que la scène soit disponible
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        // Raccourci Ctrl+Shift+A pour admin
        if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.A) {
            showAdminDashboard();
            event.consume(); // Empêche la propagation de l'événement
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

            mainContainer.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showForumDetails(Forum forum) {
        this.currentForum = forum;
        try {
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

            // Option 1: Remplace la vue actuelle
            mainContainer.getChildren().setAll(view);

            // Option 2: Ouvre dans une nouvelle fenêtre (décommenter)
            /*
            Stage adminStage = new Stage();
            adminStage.setScene(new Scene(view));
            adminStage.setTitle("Admin Dashboard");
            adminStage.initOwner(mainContainer.getScene().getWindow());
            adminStage.show();
            */
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dashboard admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}