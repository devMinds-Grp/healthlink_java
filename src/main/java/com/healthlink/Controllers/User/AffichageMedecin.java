package com.healthlink.Controllers.User;

import com.healthlink.Entites.Utilisateur;
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

public class AffichageMedecin implements Initializable {

    private final UserService userService = new UserService();
    private final ObservableList<Utilisateur> medecinList = FXCollections.observableArrayList();

    // Éléments pour les médecins
    @FXML private TableView<Utilisateur> medecinTableView;
    @FXML private TableColumn<Utilisateur, String> nomMedecinColumn;
    @FXML private TableColumn<Utilisateur, String> prenomMedecinColumn;
    @FXML private TableColumn<Utilisateur, String> emailMedecinColumn;
    @FXML private TableColumn<Utilisateur, Integer> numTelMedecinColumn;
    @FXML private TableColumn<Utilisateur, String> specialiteColumn;
    @FXML private TableColumn<Utilisateur, String> adresseColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsMedecinColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureMedecinColumns();
        setupActionsColumn();
        loadMedecins();
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


    private void setupActionsColumn() {
        actionsMedecinColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox hbox = new HBox(btnVoir, btnModifier, btnSupprimer);

            {
                hbox.setSpacing(5);
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnVoir.setOnAction(event -> voirMedecin(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(event -> modifierMedecin(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(event -> supprimerMedecin(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void loadMedecins() {
        medecinList.clear();
        List<Utilisateur> medecins = userService.findAllMedecins();
        medecinList.addAll(medecins);
        medecinTableView.setItems(medecinList);
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

    @FXML
    private void showAttenteView() {
        loadView("/views/User/enattente.fxml");
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
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}