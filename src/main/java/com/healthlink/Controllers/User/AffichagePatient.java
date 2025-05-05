package com.healthlink.Controllers.User;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.web.WebView;

public class AffichagePatient implements Initializable {
    @FXML
    private WebView webView;
    private final UserService userService = new UserService();
    private final ObservableList<Utilisateur> patientList = FXCollections.observableArrayList();

    // Éléments pour les patients
    @FXML private TableView<Utilisateur> patientTableView;
    @FXML private TableColumn<Utilisateur, String> nomPatientColumn;
    @FXML private TableColumn<Utilisateur, String> prenomPatientColumn;
    @FXML private TableColumn<Utilisateur, String> emailPatientColumn;
    @FXML private TableColumn<Utilisateur, Integer> numTelPatientColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsPatientColumn;
    @FXML private MenuItem patientsMenuItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurePatientColumns();
        setupActionsColumn();
        loadPatients();
    }

    private void configurePatientColumns() {
        nomPatientColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomPatientColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailPatientColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        numTelPatientColumn.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
    }

    private void setupActionsColumn() {
        actionsPatientColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox hbox = new HBox(btnVoir, btnModifier, btnSupprimer);

            {
                hbox.setSpacing(5);
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnVoir.setOnAction(event -> voirPatient(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(event -> modifierPatient(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(event -> supprimerPatient(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void loadPatients() {
        patientList.clear();
        List<Utilisateur> patients = userService.findAllPatients();
        patientList.addAll(patients);
        patientTableView.setItems(patientList);
    }

    private void voirPatient(Utilisateur patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/details.fxml"));
            Parent root = loader.load();

            Details controller = loader.getController();
            controller.initData(patient);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du Patient");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void modifierPatient(Utilisateur patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/modifier.fxml"));
            Parent root = loader.load();

            Modifier controller = loader.getController();
            controller.initData(patient);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Patient");
            stage.showAndWait();

            loadPatients();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerPatient(Utilisateur patient) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le patient");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce patient?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.deletePatient(patient.getId());
            loadPatients();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Patient supprimé avec succès");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void showPatientsView() {
        loadView("/views/User/list.fxml");
    }

    @FXML
    private void showMedecinsView() {
        loadView("/views/User/listMedecin.fxml");
    }

    @FXML
    private void showSoignantView() {
        loadView("/views/User/listSoignant.fxml");
    }

    @FXML
    private void showAttenteView() {
        loadView("/views/User/enattente.fxml");
    }

    @FXML
    private void showDashView(ActionEvent actionEvent) {
        loadView("/views/User/Dashboard.fxml");
    }

    @FXML
    private void showForumView() {
        loadView("/views/admindashboard.fxml");
    }

    @FXML
    private void showReclamationView() {
        loadView2("/views/list_reclamations_admin.fxml");
    }

    @FXML
    private void showCategorieView() {
        loadView2("/liste_categories.fxml");
    }

    @FXML
    private void showStatsReclamation() {
        loadView2("/views/stats.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            // Obtenir la fenêtre actuelle à partir de n'importe quel nœud de la scène
            Stage stage = (Stage) patientTableView.getScene().getWindow(); // ou tout autre nœud
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView2(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setMaxHeight(700); // 800 pixels ou la valeur de votre choix
            // Optionnel: définir aussi une largeur maximale
            stage.setMaxWidth(1200);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue: " + fxmlPath);
        }
    }

    @FXML
    private void openAddPatientForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/ajout.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau patient");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void returnToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            Parent root = loader.load();

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Changer la scène
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil");
        }
    }
}