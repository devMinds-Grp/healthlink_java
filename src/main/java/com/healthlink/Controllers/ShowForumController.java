package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Entities.ForumResponse;
import com.healthlink.Services.ForumResponseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Pair;
import java.util.Date;
import java.util.Optional;
import javafx.geometry.Insets;

public class ShowForumController {
    // Déclaration des éléments FXML
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

    private Forum forum;
    private int currentUserId = 1;
    private MainController mainController;
    private final ForumResponseService responseService = new ForumResponseService();
    private final ObservableList<ForumResponse> commentsData = FXCollections.observableArrayList();

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
        loadComments();
    }

    @FXML
    public void initialize() {
        commentColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        commentDateColumn.setCellValueFactory(cellData -> cellData.getValue().formattedDateProperty());
        actionsColumn.setCellFactory(createActionCellFactory());
        addCommentButton.setOnAction(event -> handleAddComment());
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

private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

@FXML
private void handleBackToList() {
    mainController.showForumList();
}
}