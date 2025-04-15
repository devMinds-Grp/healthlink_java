package com.healthlink.Controllers;

import com.healthlink.Entites.User;
import com.healthlink.Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AffichagePatient implements Initializable {

    private final UserService userService = new UserService();
    private final ObservableList<User> patientList = FXCollections.observableArrayList();

    // Éléments pour les patients
    @FXML private TableView<User> patientTableView;
    @FXML private TableColumn<User, String> nomPatientColumn;
    @FXML private TableColumn<User, String> prenomPatientColumn;
    @FXML private TableColumn<User, String> emailPatientColumn;
    @FXML private TableColumn<User, Integer> numTelPatientColumn;
    @FXML private TableColumn<User, Void> actionsPatientColumn;

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
        // Configuration des actions si nécessaire
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
        List<User> patients = userService.findAllPatients();
        patientList.addAll(patients);
        patientTableView.setItems(patientList);
    }

    private void voirPatient(User patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/details.fxml"));
            Parent root = loader.load();

            // Passez le patient au contrôleur des détails si nécessaire
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

    private void modifierPatient(User patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/modifier.fxml"));
            Parent root = loader.load();

            Modifier controller = loader.getController();
            controller.initData(patient);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Patient");
            stage.showAndWait(); // Attend la fermeture de la fenêtre

            // Rafraîchir la table après modification
            loadPatients();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerPatient(User patient) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le patient");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce patient?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.deletePatient(patient.getId());
            loadPatients(); // Rafraîchir la liste
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

    @FXML private MenuItem patientsMenuItem;
    @FXML private MenuItem medecinsMenuItem;
    @FXML private MenuItem soignantsMenuItem;

    // Méthode appelée quand on clique sur "Patients"
    @FXML
    private void showPatientsView() {
        loadView("/views/User/list.fxml");
    }

    // Méthode appelée quand on clique sur "Médecins"
    @FXML
    private void showMedecinsView() {
        loadView("/views/User/listMedecin.fxml");
    }
    // Méthode appelée quand on clique sur "Médecins"
    @FXML
    private void showSoignantView() {
        loadView("/views/User/listSoignant.fxml");
    }

    // Méthode pour charger les différentes vues
    private void loadView(String fxmlPath) {
        try {
            // 1. Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 2. Récupérer la fenêtre actuelle
            Stage stage = (Stage) patientsMenuItem.getParentPopup().getOwnerWindow();

            // 3. Changer la scène
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la vue: " + fxmlPath);
        }
    }
    @FXML
    private void openAddPatientForm() {
        try {
            // Charger le fichier FXML du formulaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/ajout.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau patient");
            stage.setScene(new Scene(root));

            // Configurer comme fenêtre modale
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Afficher la fenêtre et attendre sa fermeture
            stage.showAndWait();

            // Rafraîchir la table après fermeture du formulaire
            //refreshPatientTable();

        } catch (Exception e) {
            e.printStackTrace();
            // Gérer l'erreur (afficher un message à l'utilisateur)
        }
    }
}