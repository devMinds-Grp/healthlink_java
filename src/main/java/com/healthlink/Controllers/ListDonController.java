package com.healthlink.Controllers;

import com.healthlink.Entites.DonDuSang;
import com.healthlink.Services.DonDuSangService;
import com.healthlink.utils.MyDB;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import java.time.LocalDate;

public class ListDonController {

    @FXML
    private TableView<DonDuSang> donationTable;

    @FXML
    private TableColumn<DonDuSang, Integer> idColumn;

    @FXML
    private TableColumn<DonDuSang, String> descriptionColumn;

    @FXML
    private TableColumn<DonDuSang, String> lieuColumn;

    @FXML
    private TableColumn<DonDuSang, LocalDate> dateColumn;

    @FXML
    private TableColumn<DonDuSang, String> numTelColumn;

    @FXML
    private TableColumn<DonDuSang, Void> actionsColumn;

    private DonDuSangService donDuSangService;
    private ObservableList<DonDuSang> donationList;

    @FXML
    public void initialize() {
        try {
            Connection connection = MyDB.getInstance().getConnection();
            if (connection == null) {
                throw new SQLException("Failed to establish database connection");
            }
            donDuSangService = new DonDuSangService(connection);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database: " + e.getMessage());
            return;
        }

        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        lieuColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLieu()));
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
        numTelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumTel()));

        // Set up actions column with Edit and Delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox hbox = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #60a5fa; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                editButton.setOnAction(event -> {
                    DonDuSang don = getTableView().getItems().get(getIndex());
                    if (don == null) {
                        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a donation to edit.");
                        return;
                    }
                    openEditDonWindow(don);
                });
                deleteButton.setOnAction(event -> {
                    DonDuSang don = getTableView().getItems().get(getIndex());
                    if (don == null) {
                        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a donation to delete.");
                        return;
                    }
                    deleteDonation(don);
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
    private void openAddDonWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add_don.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add New Blood Donation");
            stage.showAndWait();
            refreshTable(); // Refresh the table after adding a new donation
        } catch (IOException e) {
            System.err.println("Error opening add window: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add window: " + e.getMessage());
        }
    }

    private void openEditDonWindow(DonDuSang don) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/edit_don.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edit Blood Donation");

            EditDonController controller = loader.getController();
            controller.setDonDuSang(don); // Pass the selected donation to the edit controller
            stage.showAndWait();
            refreshTable(); // Refresh the table after editing
        } catch (IOException e) {
            System.err.println("Error opening edit window: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit window: " + e.getMessage());
        }
    }

    private void deleteDonation(DonDuSang don) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this donation?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (donDuSangService.deleteDonDuSang(don.getId())) {
                    donationList.remove(don);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Donation deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete donation from the database.");
                }
            }
        });
    }

    @FXML
    private void refreshTable() {
        try {
            donationList = FXCollections.observableArrayList(donDuSangService.getAllDonDuSang());
            donationTable.setItems(donationList);
        } catch (Exception e) {
            System.err.println("Error refreshing table: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh donation list: " + e.getMessage());
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