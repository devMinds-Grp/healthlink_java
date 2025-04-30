package com.healthlink.Controllers.User;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class affichage implements Initializable {

    private UserService userService = new UserService();

    private final ObservableList<Utilisateur> patientList = FXCollections.observableArrayList();
    private final ObservableList<Utilisateur> medecinList = FXCollections.observableArrayList();
    private final ObservableList<Utilisateur> soignantList = FXCollections.observableArrayList();

    // Éléments pour les patients
    @FXML private TableView<Utilisateur> patientTableView;
    @FXML private TableColumn<Utilisateur, String> nomPatientColumn;
    @FXML private TableColumn<Utilisateur, String> prenomPatientColumn;
    @FXML private TableColumn<Utilisateur, String> emailPatientColumn;
    @FXML private TableColumn<Utilisateur, Integer> numTelPatientColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsPatientColumn;


    // Éléments pour les médecins
    @FXML private TableView<Utilisateur> medecinTableView;
    @FXML private TableColumn<Utilisateur, String> nomMedecinColumn;
    @FXML private TableColumn<Utilisateur, String> prenomMedecinColumn;
    @FXML private TableColumn<Utilisateur, String> emailMedecinColumn;
    @FXML private TableColumn<Utilisateur, Integer> numTelMedecinColumn;
    @FXML private TableColumn<Utilisateur, String> specialiteColumn;
    @FXML private TableColumn<Utilisateur, String> adresseColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsMedecinColumn;

    // Éléments pour les soignants
    @FXML private TableView<Utilisateur> soignantTableView;
    @FXML private TableColumn<Utilisateur, String> nomSoignantColumn;
    @FXML private TableColumn<Utilisateur, String> prenomSoignantColumn;
    @FXML private TableColumn<Utilisateur, String> emailSoignantColumn;
    @FXML private TableColumn<Utilisateur, String> categorieSoinColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsSoignantColumn;




    @Override
    public void initialize(URL location, ResourceBundle resources) {


        nomPatientColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomPatientColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailPatientColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        numTelPatientColumn.setCellValueFactory(new PropertyValueFactory<>("num_tel"));

        // Configuration de la colonne Actions
        setupActionsColumn();

        // Chargement des patients
        loadPatients();
        // Configuration des patients si la vue existe
        if (patientTableView != null) {
            configurePatientColumns();
            loadPatients();
        }

        if (medecinTableView != null) {
            configureMedecinColumns();
            setupMedecinActionsColumn();
            loadMedecins();
        }

        // Configuration des soignants si la vue existe
        if (soignantTableView != null) {
            configureSoignantColumns();
            loadSoignants();
        }

    }
    private void setupActionsColumn() {
        actionsPatientColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox hbox = new HBox(btnVoir, btnModifier, btnSupprimer);

            {
                // Style des boutons
                hbox.setSpacing(5);
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                // Actions des boutons
                btnVoir.setOnAction(event -> {
                    Utilisateur patient = getTableView().getItems().get(getIndex());
                    voirPatient(patient);
                });

                btnModifier.setOnAction(event -> {
                    Utilisateur patient = getTableView().getItems().get(getIndex());
                    modifierPatient(patient);
                });

                btnSupprimer.setOnAction(event -> {
                    Utilisateur patient = getTableView().getItems().get(getIndex());
                    supprimerPatient(patient);
                });
            }

            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });


    }



    private void voirPatient(Utilisateur patient) {
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

    private void modifierPatient(Utilisateur patient) {
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

    private void supprimerPatient(Utilisateur patient) {
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

    private void configurePatientColumns() {
        nomPatientColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomPatientColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailPatientColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        numTelPatientColumn.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
        // Configuration des actions si nécessaire
    }

    private void configureMedecinColumns() {
        nomMedecinColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomMedecinColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailMedecinColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        numTelMedecinColumn.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
        specialiteColumn.setCellValueFactory(new PropertyValueFactory<>("speciality"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        // Configuration des actions si nécessaire
    }


    private void configureSoignantColumns() {
        nomSoignantColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomSoignantColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailSoignantColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        categorieSoinColumn.setCellValueFactory(new PropertyValueFactory<>("categorie_soin"));
        // Configuration des actions si nécessaire
    }

    private void loadPatients() {
        patientList.clear();
        List<Utilisateur> patients = userService.findAllPatients();
        patientList.addAll(patients);
        patientTableView.setItems(patientList);
    }

    private void loadMedecins() {
        medecinList.clear();
        List<Utilisateur> medecins = userService.findAllMedecins();
        medecinList.addAll(medecins);
        medecinTableView.setItems(medecinList);
    }

    private void loadSoignants() {
        soignantList.clear();
        List<Utilisateur> soignants = userService.findAllSoignants();
        soignantList.addAll(soignants);
        soignantTableView.setItems(soignantList);
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
    @FXML
    private void openAddMedecinForm() {
        try {
            // Charger le fichier FXML du formulaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/ajoutMedecin.fxml"));
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
    @FXML
    private void openAddSoignantForm() {
        try {
            // Charger le fichier FXML du formulaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/ajoutSoignant.fxml"));
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

    private void setupMedecinActionsColumn() {
        actionsMedecinColumn.setCellFactory(param -> new TableCell<Utilisateur, Void>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox hbox = new HBox(btnVoir, btnModifier, btnSupprimer);

            {

                hbox.setSpacing(5);
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnVoir.setOnAction(event -> {
                    Utilisateur medecin = getTableView().getItems().get(getIndex());
                    voirMedecin(medecin);
                });

                btnModifier.setOnAction(event -> {
                    Utilisateur medecin = getTableView().getItems().get(getIndex());
                    modifierMedecin(medecin);
                });

                btnSupprimer.setOnAction(event -> {
                    Utilisateur medecin = getTableView().getItems().get(getIndex());
                    supprimerMedecin(medecin);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void voirMedecin(Utilisateur medecin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/detailsMedecin.fxml"));
            Parent root = loader.load();

            DetailsMedecin controller = loader.getController();
            controller.initData(medecin);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du Médecin");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails du médecin");
        }
    }

    private void modifierMedecin(Utilisateur medecin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/modifierMedecin.fxml"));
            Parent root = loader.load();

            ModifierMedecin controller = loader.getController();
            controller.initData(medecin);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Médecin");
            stage.showAndWait();

            loadMedecins(); // Rafraîchir après modification
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'éditeur de médecin");
        }
    }

    private void supprimerMedecin(Utilisateur medecin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le médecin");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce médecin?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.deleteMedecin(medecin.getId());
            loadMedecins(); // Rafraîchir la liste
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin supprimé avec succès");
        }
    }
}