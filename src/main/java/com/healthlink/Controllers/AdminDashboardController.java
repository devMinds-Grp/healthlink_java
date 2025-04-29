package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Entities.ForumResponse;
import com.healthlink.Services.ForumService;
import com.healthlink.Services.ForumResponseService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

import java.sql.Date;
import java.util.Optional;

public class AdminDashboardController {
    @FXML private TableView<Forum> pendingForumsTable;
    @FXML private TableView<ForumResponse> commentsTable;

    @FXML private TableColumn<Forum, String> forumTitleCol;
    @FXML private TableColumn<Forum, Integer> forumAuthorCol;
    @FXML private TableColumn<Forum, Date> forumDateCol;
    @FXML private TableColumn<Forum, Boolean> forumStatusCol;
    @FXML private TableColumn<Forum, Void> forumActionsCol;

    @FXML private TableColumn<ForumResponse, String> commentContentCol;
    @FXML private TableColumn<ForumResponse, Date> commentDateCol;
    @FXML private TableColumn<ForumResponse, Void> commentActionCol;

    @FXML private Tab commentsTab;

    private final ForumService forumService = new ForumService();
    private final ForumResponseService responseService = new ForumResponseService();

    @FXML
    public void initialize() {
        configureForumTableColumns();
        configureCommentTableColumns();
        refreshForumTable();
        setupTableSelectionListener();
    }

    private void configureForumTableColumns() {
        forumTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        forumAuthorCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());
        forumDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        forumStatusCol.setCellValueFactory(new PropertyValueFactory<>("approved"));

        forumStatusCol.setCellFactory(column -> new TableCell<Forum, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                // Effacer toutes les classes de style précédentes
                getStyleClass().removeAll("status-approved", "status-pending");

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Approuvé" : "En attente");
                    getStyleClass().add(item ? "status-approved" : "status-pending");
                }
            }
        });

        forumActionsCol.setCellFactory(param -> new TableCell<Forum, Void>() {
            private final HBox pane = new HBox(5);
            private final Button viewCommentsBtn = new Button("Voir commentaires");
            private final Button approveBtn = new Button("Approuver");
            private final Button rejectBtn = new Button("Désapprouver");
            private final Button deleteBtn = new Button("Supprimer");

            {
                // Styles des boutons
                viewCommentsBtn.getStyleClass().add("info-btn");
                approveBtn.getStyleClass().add("success-btn");
                rejectBtn.getStyleClass().add("warning-btn");
                deleteBtn.getStyleClass().add("danger-btn");

                // Actions des boutons
                viewCommentsBtn.setOnAction(e -> {
                    Forum forum = getTableRow().getItem();
                    if (forum != null) {
                        loadCommentsForForum(forum.getId());
                        getTableView().getSelectionModel().select(forum);
                        commentsTab.getTabPane().getSelectionModel().select(commentsTab);
                    }
                });

                approveBtn.setOnAction(e -> {
                    Forum forum = getTableRow().getItem();
                    if (forum != null) {
                        approveForum(forum);
                    }
                });

                rejectBtn.setOnAction(e -> {
                    Forum forum = getTableRow().getItem();
                    if (forum != null) {
                        rejectForum(forum);
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Forum forum = getTableRow().getItem();
                    if (forum != null) {
                        deleteForum(forum);
                    }
                });

                pane.getChildren().addAll(viewCommentsBtn, approveBtn, rejectBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Forum forum = getTableRow().getItem();
                    approveBtn.setDisable(forum.isApproved());
                    rejectBtn.setDisable(!forum.isApproved());
                    setGraphic(pane);
                }
            }
        });
    }

    private void configureCommentTableColumns() {
        commentContentCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        commentDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        commentActionCol.setCellFactory(param -> new TableCell<ForumResponse, Void>() {
            private final Button deleteBtn = new Button("Supprimer");

            {
                deleteBtn.getStyleClass().add("danger-btn");
                deleteBtn.setOnAction(e -> {
                    ForumResponse comment = getTableRow().getItem();
                    if (comment != null) {
                        deleteComment(comment);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : deleteBtn);
            }
        });
    }

    private void refreshForumTable() {
        // Sauvegarder l'ID du forum sélectionné
        int selectedId = -1;
        Forum selected = pendingForumsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedId = selected.getId();
        }

        // Recharger les données
        pendingForumsTable.setItems(FXCollections.observableArrayList(forumService.findAllForums()));

        // Forcer le rafraîchissement visuel des colonnes
        pendingForumsTable.refresh();

        // Restaurer la sélection si possible
        if (selectedId != -1) {
            for (Forum forum : pendingForumsTable.getItems()) {
                if (forum.getId() == selectedId) {
                    pendingForumsTable.getSelectionModel().select(forum);
                    break;
                }
            }
        }
    }

    private void setupTableSelectionListener() {
        pendingForumsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadCommentsForForum(newSelection.getId());
                    }
                });
    }

    private void loadCommentsForForum(int forumId) {
        commentsTable.setItems(FXCollections.observableArrayList(
                responseService.findAllCommentsByForumId(forumId)
        ));
    }

    public void approveForum(Forum forum) {
        forumService.approveForum(forum.getId());

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Succès");
        success.setHeaderText("Forum approuvé");
        success.setContentText("Le forum \"" + forum.getTitle() + "\" a été approuvé avec succès.");
        success.showAndWait();

        refreshForumTable();
    }

    public void rejectForum(Forum forum) {
        forumService.disapproveForum(forum.getId());

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Succès");
        success.setHeaderText("Forum désapprouvé");
        success.setContentText("Le forum \"" + forum.getTitle() + "\" a été désapprouvé.");
        success.showAndWait();

        refreshForumTable();
    }

    public void deleteForum(Forum forum) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le forum : " + forum.getTitle());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement ce forum et tous ses commentaires ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            forumService.deleteForumWithResponses(forum);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText("Forum supprimé");
            success.setContentText("Le forum \"" + forum.getTitle() + "\" a été supprimé avec succès.");
            success.showAndWait();

            refreshForumTable();
            commentsTable.getItems().clear();
        }
    }

    public void deleteComment(ForumResponse comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le commentaire");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement ce commentaire ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            responseService.delete(comment);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText("Commentaire supprimé");
            success.setContentText("Le commentaire a été supprimé avec succès.");
            success.showAndWait();

            Forum selectedForum = pendingForumsTable.getSelectionModel().getSelectedItem();
            if (selectedForum != null) {
                loadCommentsForForum(selectedForum.getId());
            }
        }
    }

    @FXML
    private void returnToUserList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/list.fxml"));
            Parent root = loader.load();

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Changer la scène
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Vous pouvez ajouter ici une alerte d'erreur si besoin
        }
    }
}