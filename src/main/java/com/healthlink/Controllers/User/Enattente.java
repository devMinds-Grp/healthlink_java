package com.healthlink.Controllers.User;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Enattente implements Initializable {
    @FXML private MenuItem patientsMenuItem;
    @FXML private MenuItem medecinsMenuItem;
    @FXML private MenuItem soignantsMenuItem;

    @FXML
    private TableView<Utilisateur> tableUtilisateurs;

    @FXML
    private TableColumn<Utilisateur, String> colNom;
    @FXML
    private TableColumn<Utilisateur, String> colPrenom;
    @FXML
    private TableColumn<Utilisateur, String> colEmail;
    @FXML
    private TableColumn<Utilisateur, String> colStatut;
    @FXML
    private TableColumn<Utilisateur, Void> actionsColumn;

    private UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Ajouter les boutons d'action
        addActionButtonsToTable();

        // Charger les utilisateurs en attente
        loadUtilisateursEnAttente();
    }

    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(new Callback<TableColumn<Utilisateur, Void>, TableCell<Utilisateur, Void>>() {
            @Override
            public TableCell<Utilisateur, Void> call(final TableColumn<Utilisateur, Void> param) {
                return new TableCell<Utilisateur, Void>() {
                    private final Button approveBtn = new Button("Approuver");
                    private final Button deleteBtn = new Button("Supprimer");
                    private final HBox pane = new HBox(approveBtn, deleteBtn);

                    {
                        pane.setSpacing(5);
                        approveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        approveBtn.setOnAction(event -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            approuverUtilisateur(utilisateur);
                        });

                        deleteBtn.setOnAction(event -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            supprimerUtilisateur(utilisateur);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
    }

    private void approuverUtilisateur(Utilisateur utilisateur) {
        try {
            userService.updateStatut(utilisateur.getId(), "approuvé");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("L'utilisateur a été approuvé avec succès!");
            alert.showAndWait();
            loadUtilisateursEnAttente(); // Rafraîchir la liste
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'approbation de l'utilisateur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void supprimerUtilisateur(Utilisateur utilisateur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'utilisateur");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                userService.delete(utilisateur.getId());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("L'utilisateur a été supprimé avec succès!");
                alert.showAndWait();
                loadUtilisateursEnAttente(); // Rafraîchir la liste
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void loadUtilisateursEnAttente() {
        List<Utilisateur> utilisateurs = userService.findEnAttente();
        tableUtilisateurs.getItems().setAll(utilisateurs);
    }

    // Méthode pour charger les différentes vues
    private void loadView(String fxmlPath) {
        try {
            // 1. Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 2. Récupérer la fenêtre actuelle
            Stage stage = (Stage) tableUtilisateurs.getScene().getWindow();

            // 3. Changer la scène
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la vue: " + fxmlPath);
        }
    }
    @FXML
    private void showForumView() {
        loadView("/views/admindashboard.fxml");
    }
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
    // Méthode appelée quand on clique sur "Soignant"
    @FXML
    private void showSoignantView() {
        loadView("/views/User/listSoignant.fxml");
    }
    @FXML
    public void showDashView(ActionEvent actionEvent) {
        loadView("/views/User/Dashboard.fxml");
    }
}

