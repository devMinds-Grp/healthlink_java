package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Services.ForumService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        newForumButton.setOnAction(event -> mainController.showCreateForum());
        loadApprovedForums();
    }

    @FXML
    private void handleCreateNew() {
        mainController.showCreateForum();
    }

    @FXML
    private void handleGenerateForumClick(ActionEvent event) {
        mainController.showGenerateForum();
    }

    private void loadApprovedForums() {
        forumContainer.getChildren().clear();
        forumData.setAll(forumService.findApprovedForums());
        for (Forum forum : forumData) {
            forumContainer.getChildren().add(createForumCard(forum));
        }
    }

    private HBox createForumCard(Forum forum) {
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

        VBox content = new VBox();
        content.setSpacing(5);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label titleLabel = new Label(forum.getTitle());
        titleLabel.getStyleClass().add("forum-title");

        Label descLabel = new Label(forum.getDescription());
        descLabel.getStyleClass().add("forum-description");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(3 * 14 * 1.2);
        descLabel.setStyle("-fx-overflow: hidden; -fx-text-overflow: ellipsis;");

        HBox metaInfo = new HBox(20);
        metaInfo.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(forum.getFormattedDate());
        dateLabel.getStyleClass().add("forum-date");

        HBox ratingBox = new HBox(5);
        ratingBox.getStyleClass().add("star-rating");
        double averageRating = forumService.getAverageRating(forum.getId());
        int fullStars = (int) averageRating;
        double fractionalPart = averageRating - fullStars;

        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView();
            star.setFitWidth(16);
            star.setFitHeight(16);
            if (i <= fullStars) {
                star.setImage(new Image(getClass().getResourceAsStream("/img/star_filled.png")));
            } else if (i == fullStars + 1 && fractionalPart > 0) {
                star.setImage(new Image(getClass().getResourceAsStream("/img/star_half.png")));
            } else {
                star.setImage(new Image(getClass().getResourceAsStream("/img/star_empty.png")));
            }
            ratingBox.getChildren().add(star);
        }

        Label ratingLabel = new Label(String.format("%.1f/5", averageRating));
        ratingLabel.getStyleClass().add("forum-rating");

        metaInfo.getChildren().addAll(dateLabel, ratingBox, ratingLabel);
        content.getChildren().addAll(titleLabel, descLabel, metaInfo);

        HBox actions = new HBox();
        actions.setSpacing(10);
        actions.setAlignment(Pos.CENTER);

        Button showBtn = new Button();
        showBtn.getStyleClass().addAll("icon-button", "show-icon-button");
        showBtn.setOnAction(e -> mainController.showForumDetails(forum));

        // Ajouter une icône au bouton
        ImageView showIcon = new ImageView(new Image(getClass().getResourceAsStream("/img/show2.png")));
        showIcon.setFitWidth(32);
        showIcon.setFitHeight(32);
        showBtn.setGraphic(showIcon);

        actions.getChildren().add(showBtn);
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

    public void setShowOnlyApproved(boolean showOnlyApproved) {
        refreshTable();
    }

    public void toggleWelcomeSection(boolean show) {
        welcomeSection.setVisible(show);
        welcomeSection.setManaged(show);
    }
}
