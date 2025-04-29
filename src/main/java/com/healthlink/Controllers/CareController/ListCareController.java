package com.healthlink.Controllers.CareController;

import com.healthlink.Controllers.CareRespController.AddCareResponseController;
import com.healthlink.Controllers.CareRespController.ListCareResponsesController;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entities.Care;
import com.healthlink.Services.AuthService;
import com.healthlink.Services.CareService;
import com.healthlink.utils.MyDB;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ListCareController {

    @FXML private TableView<Care> careTable;
    @FXML private TableColumn<Care, Void> actionsColumn;

    private final CareService careService;

    public ListCareController() {
        try {
            Connection connection = MyDB.getInstance().getConnection();
            this.careService = new CareService(connection);
            System.out.println("ListCareController initialized successfully");
        } catch (SQLException e) {
            System.err.println("Failed to initialize ListCareController: " + e.getMessage());
            throw new RuntimeException("Failed to initialize CareService", e);
        }
    }

    @FXML
    public void initialize() {
        try {
            // Load care records
            refreshTable();
            // Set up Actions column with Update, Delete, Response, and View Responses buttons
            actionsColumn.setCellFactory(param -> new TableCell<Care, Void>() {
                private final Button updateButton = new Button("Update");
                private final Button deleteButton = new Button("Delete");
                private final Button responseButton = new Button("Response");
                private final Button viewResponsesButton = new Button("View Responses");

                {
                    updateButton.getStyleClass().add("edit-button");
                    deleteButton.getStyleClass().add("delete-button");
                    responseButton.getStyleClass().add("response-button");
                    viewResponsesButton.getStyleClass().add("view-responses-button");
                    updateButton.setOnAction(event -> {
                        Care care = getTableView().getItems().get(getIndex());
                        if (care != null) {
                            goToUpdateCare(care);
                        }
                    });
                    deleteButton.setOnAction(event -> {
                        Care care = getTableView().getItems().get(getIndex());
                        if (care != null) {
                            handleDelete(care);
                        }
                    });
                    responseButton.setOnAction(event -> {
                        Care care = getTableView().getItems().get(getIndex());
                        if (care != null) {
                            goToAddCareResponse(care);
                        }
                    });
                    viewResponsesButton.setOnAction(event -> {
                        Care care = getTableView().getItems().get(getIndex());
                        if (care != null) {
                            goToListCareResponses(care);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox buttonBox = new HBox(10, updateButton, deleteButton, viewResponsesButton);
                        Utilisateur utilisateur = AuthService.getConnectedUtilisateur();
                        if (utilisateur != null && utilisateur.getRole().getId() == 4) { // ROLE_SOIGNANT
                            buttonBox.getChildren().add(2, responseButton);
                        }
                        setGraphic(buttonBox);
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to initialize care table: " + e.getMessage());
            showAlert("Error", "Failed to load care records: " + e.getMessage());
        }
    }

    @FXML
    private void goToAddCare() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/care/AddCare.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Care");
            stage.setScene(new Scene(root, 800, 600));
            stage.setOnHidden(event -> refreshTable());
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load AddCare.fxml: " + e.getMessage());
            showAlert("Error", "Failed to open Add Care form: " + e.getMessage());
        }
    }

    private void goToUpdateCare(Care care) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/care/UpdateCare.fxml"));
            Parent root = loader.load();
            UpdateCareController controller = loader.getController();
            controller.setCare(care);
            Stage stage = new Stage();
            stage.setTitle("Update Care");
            stage.setScene(new Scene(root, 800, 600));
            stage.setOnHidden(event -> refreshTable());
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load UpdateCare.fxml: " + e.getMessage());
            showAlert("Error", "Failed to open Update Care form: " + e.getMessage());
        }
    }

    private void goToAddCareResponse(Care care) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/careResp/AddCareResponse.fxml"));
            Parent root = loader.load();
            AddCareResponseController controller = loader.getController();
            controller.setCareId(care.getId());
            Stage stage = new Stage();
            stage.setTitle("Add Care Response");
            stage.setScene(new Scene(root, 800, 600));
            stage.setOnHidden(event -> refreshTable());
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load AddCareResponse.fxml: " + e.getMessage());
            showAlert("Error", "Failed to open Add Care Response form: " + e.getMessage());
        }
    }

    private void goToListCareResponses(Care care) {
        try {
            Utilisateur currentUser = AuthService.getConnectedUtilisateur();
            System.out.println("Loading ListCareResponses.fxml for care ID=" + care.getId() + ", user ID=" + (currentUser != null ? currentUser.getId() : "null"));
            String fxmlPath = "/views/careResp/ListCareResponses.fxml";
            System.out.println("Attempting to load FXML from path: " + fxmlPath);
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found at path: " + fxmlPath);
            }
            System.out.println("FXML URL resolved to: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            System.out.println("FXMLLoader created with URL: " + fxmlUrl);
            Parent root = loader.load();
            System.out.println("ListCareResponses.fxml loaded successfully");
            ListCareResponsesController controller = loader.getController();
            System.out.println("ListCareResponsesController instance retrieved: " + controller);
            controller.setCareId(care.getId());
            Stage stage = new Stage();
            stage.setTitle("Care Responses");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load ListCareResponses.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open Care Responses list: " + e.getMessage());
        }
    }

    public void handleDelete(Care care) {
        try {
            boolean success = careService.deleteCare(care.getId());
            if (success) {
                showAlert("Success", "Care record and associated responses deleted successfully");
                refreshTable();
            } else {
                showAlert("Error", "Failed to delete care record");
                refreshTable();
            }
        } catch (Exception e) {
            System.err.println("Failed to delete care record: " + e.getMessage());
            showAlert("Error", "Failed to delete care record: " + e.getMessage());
            refreshTable();
        }
    }

    private void refreshTable() {
        try {
            List<Care> careList = careService.getAllCares();
            careTable.getItems().setAll(careList);
        } catch (Exception e) {
            System.err.println("Failed to refresh care table: " + e.getMessage());
            showAlert("Error", "Failed to refresh care records: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}