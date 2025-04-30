package com.healthlink.Controllers.CareRespController;

import com.healthlink.Entities.CareResponse;
import com.healthlink.Services.CareResponseService;
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

public class ListCareResponsesController {

    @FXML private TableView<CareResponse> responseTable;
    @FXML private TableColumn<CareResponse, Void> actionsColumn;

    private final CareResponseService responseService;
    private int careId;

    public ListCareResponsesController() {
        try {
            Connection connection = MyDB.getInstance().getConnection();
            this.responseService = new CareResponseService(connection);
            System.out.println("ListCareResponsesController initialized successfully");
        } catch (SQLException e) {
            System.err.println("Failed to initialize ListCareResponsesController: " + e.getMessage());
            throw new RuntimeException("Failed to initialize CareResponseService", e);
        }
    }

    public void setCareId(int careId) {
        this.careId = careId;
        loadResponses();
    }

    @FXML
    public void initialize() {
        try {
            // Set up Actions column with Delete and Update buttons
            actionsColumn.setCellFactory(param -> new TableCell<CareResponse, Void>() {
                private final Button deleteButton = new Button("Delete");
                private final Button updateButton = new Button("Update");
                private final HBox buttonBox = new HBox(5, deleteButton, updateButton);

                {
                    deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    updateButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                    deleteButton.setOnAction(event -> {
                        CareResponse response = getTableView().getItems().get(getIndex());
                        if (response != null) {
                            handleDelete(response);
                        }
                    });
                    updateButton.setOnAction(event -> {
                        CareResponse response = getTableView().getItems().get(getIndex());
                        if (response != null) {
                            goToUpdateCareResponse(response);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttonBox);
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to initialize response table: " + e.getMessage());
            showAlert("Error", "Failed to initialize response table: " + e.getMessage());
        }
    }

    private void loadResponses() {
        try {
            List<CareResponse> responses = responseService.getResponsesByCareId(careId);
            responseTable.getItems().setAll(responses);
            if (responses.isEmpty()) {
                showAlert("Information", "No responses found for this care record.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load responses: " + e.getMessage());
            showAlert("Error", "Failed to load responses: " + e.getMessage());
        }
    }

    private void handleDelete(CareResponse response) {
        try {
            boolean success = responseService.deleteCareResponse(response.getId());
            if (success) {
                showAlert("Success", "Care response deleted successfully");
                loadResponses();
            } else {
                showAlert("Error", "Failed to delete care response");
            }
        } catch (Exception e) {
            System.err.println("Failed to delete response: " + e.getMessage());
            showAlert("Error", "Failed to delete care response: " + e.getMessage());
        }
    }

    private void goToUpdateCareResponse(CareResponse response) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/careResp/UpdateCareResponse.fxml"));
            Parent root = loader.load();
            UpdateCareResponseController controller = loader.getController();
            controller.setCareResponse(response);
            Stage stage = new Stage();
            stage.setTitle("Update Care Response");
            stage.setScene(new Scene(root, 600, 400));
            stage.setOnHidden(event -> loadResponses());
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load UpdateCareResponse.fxml: " + e.getMessage());
            showAlert("Error", "Failed to open Update Care Response form: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        responseTable.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}