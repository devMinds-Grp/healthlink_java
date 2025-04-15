package com.healthlink.Controllers;

import com.healthlink.Entites.ReponseDon;
import com.healthlink.Services.ReponseDonService;
import com.healthlink.utils.MyDB;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class ListReponseDonController {

    @FXML
    private TableView<ReponseDon> responseTable;

    @FXML
    private TableColumn<ReponseDon, Integer> idColumn;

    @FXML
    private TableColumn<ReponseDon, String> descriptionColumn;

    @FXML
    private TableColumn<ReponseDon, Void> actionsColumn;

    private ReponseDonService reponseDonService;
    private ObservableList<ReponseDon> responseList;

    @FXML
    public void initialize() {
        try {
            Connection connection = MyDB.getInstance().getConnection();
            if (connection == null) {
                throw new SQLException("Failed to establish database connection");
            }
            reponseDonService = new ReponseDonService(connection);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database: " + e.getMessage());
            return;
        }

        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));

        // Set up actions column with Edit and Delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox hbox = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                editButton.setOnAction(event -> {
                    ReponseDon reponse = getTableView().getItems().get(getIndex());
                    if (reponse == null) {
                        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a response to edit.");
                        return;
                    }
                    openEditReponseDonWindow(reponse);
                });
                deleteButton.setOnAction(event -> {
                    ReponseDon reponse = getTableView().getItems().get(getIndex());
                    if (reponse == null) {
                        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a response to delete.");
                        return;
                    }
                    deleteReponseDon(reponse);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        // Load data into the table
        refreshTable();
    }

    @FXML
    private void openAddReponseDonWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add_reponse_don.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add New Response Donation");
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("Error opening add window: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add window: " + e.getMessage());
        }
    }

    private void openEditReponseDonWindow(ReponseDon reponse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/edit_reponse_don.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edit Response Donation");

            EditReponseDonController controller = loader.getController();
            controller.setReponseDon(reponse);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("Error opening edit window: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit window: " + e.getMessage());
        }
    }

    private void deleteReponseDon(ReponseDon reponse) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this response?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (reponseDonService.deleteReponseDon(reponse.getId())) {
                    responseList.remove(reponse);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Response deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete response from the database.");
                }
            }
        });
    }

    @FXML
    private void refreshTable() {
        try {
            responseList = FXCollections.observableArrayList(reponseDonService.getAllReponseDon());
            responseTable.setItems(responseList);
        } catch (Exception e) {
            System.err.println("Error refreshing table: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh response list: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}