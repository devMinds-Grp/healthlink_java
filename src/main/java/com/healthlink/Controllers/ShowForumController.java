package com.healthlink.Controllers;

import com.healthlink.Entities.*;
import com.healthlink.Services.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Callback;
import java.util.*;
import java.sql.Timestamp;
import javafx.scene.control.TextInputDialog;
import java.net.URI;
import com.google.gson.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import java.awt.Desktop;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class ShowForumController {
    @FXML private Label titleLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<ForumResponse> commentsTable;
    @FXML private TableColumn<ForumResponse, String> commentColumn;
    @FXML private TableColumn<ForumResponse, String> commentDateColumn;
    @FXML private TableColumn<ForumResponse, Void> actionsColumn;
    @FXML private TextArea newCommentArea;
    @FXML private Button addCommentButton;
    @FXML private HBox ratingStarsContainer;
    @FXML private TextArea ratingCommentArea;
    @FXML private Button submitRatingButton;
    @FXML private Label averageRatingLabel;
    @FXML private Button reportButton;
    @FXML private Button verifyFactButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private VBox factCheckResultsContainer;
    @FXML private VBox ratingSubmissionContainer; // Added for rating submission section

    private final ReportService reportService = new ReportService();
    private final UserService userService = new UserService();
    private final RatingService ratingService = new RatingService();
    private final FactCheckService factCheckService = new FactCheckService();
    private Forum forum;
    private int currentUserId;
    private MainController mainController;
    private final ForumResponseService responseService = new ForumResponseService();
    private final ForumService forumService = new ForumService();
    private final ObservableList<ForumResponse> commentsData = FXCollections.observableArrayList();
    private List<ImageView> starIcons = new ArrayList<>();
    private int currentRating = 0;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setForum(Forum forum, int currentUserId) {
        this.forum = forum;
        this.currentUserId = currentUserId;

        titleLabel.setText(forum.getTitle());
        descriptionArea.setText(forum.getDescription());
        dateLabel.setText(forum.getFormattedDate());
        statusLabel.setText(forum.isApproved() ? "Approuvé" : "En attente");

        // Check if the current user is the creator of the forum
        boolean isCreator = (forum.getUserId() == currentUserId);
        editButton.setVisible(isCreator);
        editButton.setManaged(isCreator);
        deleteButton.setVisible(isCreator);
        deleteButton.setManaged(isCreator);
        reportButton.setVisible(!isCreator); // Hide report button for creator
        reportButton.setManaged(!isCreator);
        verifyFactButton.setVisible(!isCreator); // Hide verify button for creator
        verifyFactButton.setManaged(!isCreator);
        ratingSubmissionContainer.setVisible(!isCreator); // Hide rating submission for creator
        ratingSubmissionContainer.setManaged(!isCreator);

        loadComments();
        initRatingSystem();
        updateReportButtonState();
    }

    @FXML
    public void initialize() {
        commentColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        commentDateColumn.setCellValueFactory(cellData -> cellData.getValue().formattedDateProperty());
        actionsColumn.setCellFactory(createActionCellFactory());

        addCommentButton.setOnAction(event -> handleAddComment());
        submitRatingButton.setOnAction(event -> handleSubmitRating());
        reportButton.setOnAction(event -> handleReportForum());
        verifyFactButton.setOnAction(event -> verifyForumContent());
    }

    private void verifyForumContent() {
        try {
            factCheckResultsContainer.getChildren().clear();
            factCheckResultsContainer.setVisible(true);

            Label headerLabel = new Label("Résultats de vérification des faits");
            headerLabel.getStyleClass().add("fact-check-header");
            factCheckResultsContainer.getChildren().add(headerLabel);

            String titleText = titleLabel.getText();
            Label titleSectionLabel = new Label("Vérification du TITRE:");
            titleSectionLabel.getStyleClass().add("fact-check-section");
            factCheckResultsContainer.getChildren().add(titleSectionLabel);

            List<Map<String, String>> titleResults = factCheckService.verifyMedicalClaim(titleText);

            if (titleResults.isEmpty()) {
                Label noTitleResultsLabel = new Label("Aucune vérification trouvée pour le titre");
                noTitleResultsLabel.getStyleClass().add("fact-check-no-result");
                factCheckResultsContainer.getChildren().add(noTitleResultsLabel);
            } else {
                addResultsToContainer(titleResults, "title");
            }

            Separator separator = new Separator();
            separator.setPadding(new Insets(10, 0, 10, 0));
            factCheckResultsContainer.getChildren().add(separator);

            String descriptionText = descriptionArea.getText();
            Label descSectionLabel = new Label("Vérification de la DESCRIPTION:");
            descSectionLabel.getStyleClass().add("fact-check-section");
            factCheckResultsContainer.getChildren().add(descSectionLabel);

            List<Map<String, String>> descriptionResults = factCheckService.verifyMedicalClaim(descriptionText);

            if (descriptionResults.isEmpty()) {
                Label noDescResultsLabel = new Label("Aucune vérification trouvée pour la description");
                noDescResultsLabel.getStyleClass().add("fact-check-no-result");
                factCheckResultsContainer.getChildren().add(noDescResultsLabel);
            } else {
                addResultsToContainer(descriptionResults, "description");
            }

            titleLabel.getStyleClass().remove("unverified-claim");
            titleLabel.getStyleClass().remove("false-claim");
            descriptionArea.getStyleClass().remove("unverified-claim");
            descriptionArea.getStyleClass().remove("false-claim");

            boolean titleHasFalseClaim = false;
            boolean titleHasUncertainClaim = false;
            boolean descHasFalseClaim = false;
            boolean descHasUncertainClaim = false;

            for (Map<String, String> result : titleResults) {
                String rating = result.getOrDefault("rating", "").toLowerCase();
                System.out.println("Évaluation du rating pour le titre : " + rating);
                if (rating.contains("faux")) {
                    System.out.println("Match trouvé : faux");
                    titleHasFalseClaim = true;
                } else if (rating.contains("false")) {
                    System.out.println("Match trouvé : false");
                    titleHasFalseClaim = true;
                } else if (rating.contains("incorrect")) {
                    System.out.println("Match trouvé : incorrect");
                    titleHasFalseClaim = true;
                } else if (rating.contains("not true")) {
                    System.out.println("Match trouvé : not true");
                    titleHasFalseClaim = true;
                } else if (rating.contains("mensonges")) {
                    System.out.println("Match trouvé : mensonges");
                    titleHasFalseClaim = true;
                } else if (rating.contains("misleading")) {
                    System.out.println("Match trouvé : misleading");
                    titleHasFalseClaim = true;
                } else if (rating.contains("inaccurate")) {
                    System.out.println("Match trouvé : inaccurate");
                    titleHasFalseClaim = true;
                } else if (rating.contains("ne prouve pas")) {
                    System.out.println("Match trouvé : ne prouve pas");
                    titleHasFalseClaim = true;
                } else if (rating.contains("ne correspond pas")) {
                    System.out.println("Match trouvé : ne correspond pas");
                    titleHasFalseClaim = true;
                } else if (rating.contains("n'est pas")) {
                    System.out.println("Match trouvé : n'est pas");
                    titleHasFalseClaim = true;
                } else if (rating.contains("n'a pas")) {
                    System.out.println("Match trouvé : n'a pas");
                    titleHasFalseClaim = true;
                } else if (rating.contains("partiellement")) {
                    System.out.println("Match trouvé : partiellement");
                    titleHasUncertainClaim = true;
                } else if (rating.contains("partially")) {
                    System.out.println("Match trouvé : partially");
                    titleHasUncertainClaim = true;
                }
            }

            for (Map<String, String> result : descriptionResults) {
                String rating = result.getOrDefault("rating", "").toLowerCase();
                if (rating.contains("faux") || rating.contains("false") || rating.contains("incorrect") ||
                        rating.contains("not true") || rating.contains("mensonges") || rating.contains("misleading") ||
                        rating.contains("inaccurate") || rating.contains("ne prouve pas") ||
                        rating.contains("ne correspond pas") || rating.contains("n'est pas")) {
                    descHasFalseClaim = true;
                } else if (rating.contains("partiellement") || rating.contains("partially")) {
                    descHasUncertainClaim = true;
                }
            }

            System.out.println("titleResults: " + titleResults);
            System.out.println("descriptionResults: " + descriptionResults);
            System.out.println("titleHasFalseClaim: " + titleHasFalseClaim + ", titleHasUncertainClaim: " + titleHasUncertainClaim);
            System.out.println("descHasFalseClaim: " + descHasFalseClaim + ", descHasUncertainClaim: " + descHasUncertainClaim);

            if (titleHasFalseClaim) {
                System.out.println("Application du style false-claim au titre");
                titleLabel.getStyleClass().add("false-claim");
                System.out.println("Styles actuels du titleLabel : " + titleLabel.getStyleClass());
            } else if (titleHasUncertainClaim) {
                System.out.println("Application du style unverified-claim au titre");
                titleLabel.getStyleClass().add("unverified-claim");
                System.out.println("Styles actuels du titleLabel : " + titleLabel.getStyleClass());
            }

            if (descHasFalseClaim) {
                System.out.println("Application du style false-claim à la description");
                descriptionArea.getStyleClass().add("false-claim");
            } else if (descHasUncertainClaim) {
                System.out.println("Application du style unverified-claim à la description");
                descriptionArea.getStyleClass().add("unverified-claim");
            }

        } catch (Exception e) {
            showAlert("Erreur", "Échec de la vérification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addResultsToContainer(List<Map<String, String>> results, String source) {
        for (Map<String, String> result : results) {
            VBox resultBox = new VBox(5);
            resultBox.getStyleClass().add("fact-check-result");

            HBox ratingBox = new HBox(10);
            ratingBox.setAlignment(Pos.CENTER_LEFT);

            ImageView statusIcon = new ImageView();
            statusIcon.setFitWidth(24);
            statusIcon.setFitHeight(24);

            String rating = result.getOrDefault("rating", "").toLowerCase();
            if (rating.contains("faux") || rating.contains("false") || rating.contains("incorrect")) {
                statusIcon.setImage(new Image(getClass().getResourceAsStream("/img/false_icon.png")));
                resultBox.getStyleClass().add("false-claim");
            } else if (rating.contains("vrai") || rating.contains("true") || rating.contains("correct")) {
                statusIcon.setImage(new Image(getClass().getResourceAsStream("/img/true_icon.png")));
                resultBox.getStyleClass().add("true-claim");
            } else if (rating.contains("partially") || rating.contains("partiellement")) {
                statusIcon.setImage(new Image(getClass().getResourceAsStream("/img/partial_icon.png")));
                resultBox.getStyleClass().add("partial-claim");
            } else {
                statusIcon.setImage(new Image(getClass().getResourceAsStream("/img/unknown_icon.png")));
                resultBox.getStyleClass().add("unknown-claim");
            }

            Label ratingLabel = new Label("Évaluation: " + result.getOrDefault("rating", "Non spécifié"));
            ratingLabel.getStyleClass().add("fact-check-rating");

            ratingBox.getChildren().addAll(statusIcon, ratingLabel);

            Label claimLabel = new Label("Affirmation vérifiée: " + result.getOrDefault("text", ""));
            claimLabel.setWrapText(true);
            claimLabel.getStyleClass().add("fact-check-claim");

            Label sourceLabel = new Label("Source: " + result.getOrDefault("publisher", "Non spécifié"));
            sourceLabel.getStyleClass().add("fact-check-source");

            Button sourceLink = new Button("Voir l'article complet");
            sourceLink.getStyleClass().add("link-button");

            String url = result.getOrDefault("url", "");
            if (!url.isEmpty()) {
                sourceLink.setOnAction(e -> {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        showAlert("Erreur", "Impossible d'ouvrir le lien: " + ex.getMessage());
                    }
                });
            } else {
                sourceLink.setDisable(true);
            }

            resultBox.getChildren().addAll(ratingBox, claimLabel, sourceLabel, sourceLink);

            Label badgeLabel = new Label(source.equals("title") ? "Trouvé dans le titre" : "Trouvé dans la description");
            badgeLabel.getStyleClass().add("fact-check-source-badge");
            resultBox.getChildren().add(badgeLabel);

            factCheckResultsContainer.getChildren().add(resultBox);
        }
    }

    private boolean hasFalseClaim(List<Map<String, String>> results) {
        for (Map<String, String> result : results) {
            String rating = result.getOrDefault("rating", "").toLowerCase();
            if (rating.contains("faux") ||
                    rating.contains("false") ||
                    rating.contains("incorrect") ||
                    rating.contains("not true") ||
                    rating.contains("mensonges") ||
                    rating.contains("misleading") ||
                    rating.contains("inaccurate") ||
                    rating.contains("ne prouve pas") ||
                    rating.contains("ne correspond pas") ||
                    rating.contains("n'est pas") ||
                    rating.contains("partiellement") ||
                    rating.contains("partially")) {
                return true;
            }
        }
        return false;
    }

    private void initRatingSystem() {
        starIcons.clear();
        ratingStarsContainer.getChildren().clear();

        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView();
            star.setFitWidth(30);
            star.setFitHeight(30);
            star.setUserData(i);
            star.getStyleClass().add("rating-star");
            updateStarImage(star, i <= currentRating);

            star.setOnMouseEntered(event -> previewRating((int) star.getUserData()));
            star.setOnMouseExited(event -> resetStarDisplay());
            star.setOnMouseClicked(event -> setRating((int) star.getUserData()));

            starIcons.add(star);
            ratingStarsContainer.getChildren().add(star);
        }

        updateAverageRatingDisplay();
        updateRatingUI();
    }

    private void updateStarImage(ImageView star, boolean filled) {
        String imagePath = filled ? "/img/star_filled.png" : "/img/star_empty.png";
        star.setImage(new Image(getClass().getResourceAsStream(imagePath)));
    }

    private void previewRating(int rating) {
        for (int i = 0; i < starIcons.size(); i++) {
            ImageView star = starIcons.get(i);
            updateStarImage(star, i < rating);
        }
    }

    private void resetStarDisplay() {
        for (int i = 0; i < starIcons.size(); i++) {
            ImageView star = starIcons.get(i);
            updateStarImage(star, i < currentRating);
        }
    }

    private void setRating(int rating) {
        this.currentRating = rating;
        resetStarDisplay();
    }

    private void updateAverageRatingDisplay() {
        double averageRating = forumService.getAverageRating(forum.getId());
        HBox averageStarsBox = new HBox(5);
        averageStarsBox.getStyleClass().add("star-rating");

        int fullStars = (int) averageRating;
        double fractionalPart = averageRating - fullStars;

        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView();
            star.setFitWidth(30);
            star.setFitHeight(30);

            if (i <= fullStars) {
                updateStarImage(star, true);
            } else if (i == fullStars + 1 && fractionalPart > 0) {
                star.setImage(new Image(getClass().getResourceAsStream("/img/star_half.png")));
            } else {
                updateStarImage(star, false);
            }
            averageStarsBox.getChildren().add(star);
        }

        averageRatingLabel.setText(String.format("Note moyenne : %.1f/5", averageRating));
        averageRatingLabel.setGraphic(averageStarsBox);
    }

    private void handleSubmitRating() {
        if (currentRating == 0) {
            showAlert("Erreur", "Veuillez sélectionner une note entre 1 et 5 étoiles");
            return;
        }

        try {
            if (forumService.hasUserRatedForum(forum.getId(), currentUserId)) {
                Rating existingRating = getExistingRating();
                existingRating.setStars(currentRating);
                existingRating.setComment(ratingCommentArea.getText().trim());
                ratingService.updateRating(existingRating);
            } else {
                forumService.addRating(
                        forum.getId(),
                        currentUserId,
                        currentRating,
                        ratingCommentArea.getText().trim()
                );
            }

            updateAverageRatingDisplay();
            ratingCommentArea.clear();
            showAlert("Succès", "Votre évaluation a été enregistrée");
        } catch (Exception e) {
            showAlert("Erreur", "Échec de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Rating getExistingRating() {
        List<Rating> ratings = forumService.findRatingsByForum(forum.getId());
        for (Rating rating : ratings) {
            if (rating.getUser().getId() == currentUserId) {
                return rating;
            }
        }
        return null;
    }

    private void updateRatingUI() {
        boolean hasRated = forumService.hasUserRatedForum(forum.getId(), currentUserId);
        submitRatingButton.setText(hasRated ? "Mettre à jour votre évaluation" : "Soumettre votre évaluation");

        if (hasRated) {
            Rating existingRating = getExistingRating();
            if (existingRating != null) {
                currentRating = existingRating.getStars();
                ratingCommentArea.setText(existingRating.getComment());
                resetStarDisplay();
            }
        }
    }

    private void loadComments() {
        commentsData.setAll(responseService.findByForum(forum.getId(), 10, 0));
        commentsTable.setItems(commentsData);
    }

    private void handleAddComment() {
        String commentText = newCommentArea.getText().trim();
        if (!commentText.isEmpty()) {
            try {
                ForumResponse response = new ForumResponse(
                        commentText,
                        new Date(),
                        forum.getId()
                );
                responseService.add(response);
                newCommentArea.clear();
                loadComments();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible d'ajouter le commentaire: " + e.getMessage());
            }
        } else {
            showAlert("Erreur", "Le commentaire ne peut pas être vide");
        }
    }

    private Callback<TableColumn<ForumResponse, Void>, TableCell<ForumResponse, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    ForumResponse response = getTableView().getItems().get(getIndex());
                    handleEditComment(response);
                });

                deleteBtn.setOnAction(event -> {
                    ForumResponse response = getTableView().getItems().get(getIndex());
                    handleDeleteComment(response);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        };
    }

    private void handleEditComment(ForumResponse response) {
        mainController.showEditComment(response);
    }

    private void handleDeleteComment(ForumResponse response) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ce commentaire ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                responseService.delete(response);
                loadComments();
            }
        });
    }

    private void updateReportButtonState() {
        boolean hasReported = reportService.hasUserReportedForum(forum.getId(), currentUserId);
        reportButton.setDisable(hasReported);
        if (hasReported) {
            reportButton.setText("Déjà signalé");
            reportButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: #7f8c8d;");
        } else {
            reportButton.setText("Signaler ce forum");
            reportButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        }
    }

    private void handleReportForum() {
        if (reportService.hasUserReportedForum(forum.getId(), currentUserId)) {
            showAlert("Information", "Vous avez déjà signalé ce forum.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Signaler le forum");
        dialog.setHeaderText("Pourquoi signalez-vous ce forum?");
        dialog.setContentText("Raison:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            try {
                Report report = new Report(forum.getId(), currentUserId, new Date(), reason);
                reportService.addReport(report);

                int reportCount = reportService.countReportsForForum(forum.getId());
                if (reportCount >= 3) {
                    forumService.deleteForum(forum.getId());
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, 10);
                    Date banEnd = calendar.getTime();
                    userService.banUser(forum.getUserId(), banEnd, "Forum signalé 3 fois");
                    showAlert("Information", "Ce forum a été signalé 3 fois et a été supprimé. L'auteur a été banni jusqu'au " + banEnd);
                    mainController.showForumList();
                } else {
                    showAlert("Information", "Votre signalement a été enregistré.");
                }

                updateReportButtonState();
            } catch (Exception e) {
                showAlert("Erreur", "Une erreur s'est produite lors du signalement: " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackToList() {
        mainController.showForumList();
    }

    @FXML
    private void handleEditForum() {
        mainController.showEditForum(forum);
    }

    @FXML
    private void handleDeleteForum() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le forum : " + forum.getTitle());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement ce forum et tous ses commentaires ?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                forumService.deleteForum(forum.getId());
                mainController.showForumList();
            }
        });
    }
}