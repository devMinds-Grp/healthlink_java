package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Services.ForumService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Optional;

public class ForumListController {
    @FXML private VBox forumContainer;
    @FXML private VBox welcomeSection;
    @FXML private Button newForumButton;

    private final ForumService forumService = new ForumService();
    private final ObservableList<Forum> forumData = FXCollections.observableArrayList();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Configuration du bouton Nouveau Forum
        newForumButton.setOnAction(event -> mainController.showCreateForum());

        // Chargement des données (forums approuvés)
        loadApprovedForums();
    }

    @FXML
    private void handleCreateNew() {
        mainController.showCreateForum();
    }

    private void loadApprovedForums() {
        // Effacer les forums existants
        forumContainer.getChildren().clear();

        // Charger les forums approuvés
        forumData.setAll(forumService.findApprovedForums());

        // Créer une carte pour chaque forum
        for (Forum forum : forumData) {
            forumContainer.getChildren().add(createForumCard(forum));
        }
    }

    private HBox createForumCard(Forum forum) {
        // Création du conteneur principal
        HBox card = new HBox();
        card.getStyleClass().add("forum-card");
        card.setPadding(new Insets(15));
        card.setSpacing(15);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMinHeight(80);
        card.setBorder(new Border(new BorderStroke(Color.web("#e0e0e0"),
                BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
        card.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(4), Insets.EMPTY)));

        // Contenu du forum (titre, description, date)
        VBox content = new VBox();
        content.setSpacing(5);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label titleLabel = new Label(forum.getTitle());
        titleLabel.getStyleClass().add("forum-title");

        Label descLabel = new Label(forum.getDescription());
        descLabel.getStyleClass().add("forum-description");
        descLabel.setWrapText(true);

        Label dateLabel = new Label(forum.getFormattedDate());
        dateLabel.getStyleClass().add("forum-date");

        content.getChildren().addAll(titleLabel, descLabel, dateLabel);

        // Boutons d'action
        HBox actions = new HBox();
        actions.setSpacing(10);
        actions.setAlignment(Pos.CENTER);

        Button showBtn = new Button("Show");
        showBtn.getStyleClass().addAll("action-button", "show-button");
        showBtn.setOnAction(e -> mainController.showForumDetails(forum));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("action-button", "edit-button");
        editBtn.setOnAction(e -> mainController.showEditForum(forum));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("action-button", "delete-button");
        deleteBtn.setOnAction(e -> deleteForum(forum));

        actions.getChildren().addAll(showBtn, editBtn, deleteBtn);

        // Assemblage final
        card.getChildren().addAll(content, actions);

        return card;
    }

    public void refreshTable() {
        loadApprovedForums();
    }

    private void deleteForum(Forum forum) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le forum : " + forum.getTitle());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement ce forum et tous ses commentaires ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            forumService.deleteForumWithResponses(forum);
            refreshTable();
        }
    }

    // Méthode optionnelle pour filtrer selon le statut d'approbation
    public void setShowOnlyApproved(boolean showOnlyApproved) {
        refreshTable(); // Dans cette version, on affiche toujours seulement les approuvés
    }

    // Méthode pour basculer entre l'affichage d'accueil et la liste des forums
    public void toggleWelcomeSection(boolean show) {
        welcomeSection.setVisible(show);
        welcomeSection.setManaged(show);
    }
}