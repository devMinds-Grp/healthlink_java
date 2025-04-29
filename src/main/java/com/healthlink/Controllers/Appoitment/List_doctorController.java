package com.healthlink.Controllers.Appoitment;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class List_doctorController {

    @FXML private TextField searchField;
    @FXML private VBox specialtiesFilter;
    @FXML private VBox addressFilter;
    @FXML private Button resetFiltersButton;
    @FXML private VBox doctorList;

    private ObservableList<Doctor> doctors;
    private FilteredList<Doctor> filteredDoctors;
    private final UserService userService = new UserService();

    private static final String PROFILE_IMAGES_DIR = "profile_images";
    private static final String DEFAULT_IMAGE_PATH = "/images/default_profile.png"; // Ensure this exists in resources

    @FXML
    public void initialize() {
        try {
            loadDoctors();
            setupSearchFilter();
            setupSpecialtiesFilter();
            setupAddressFilter();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur d'initialisation", "Erreur lors de l'initialisation de la page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadDoctors() {
        List<Utilisateur> medecins = userService.findAllMedecins();
        if (medecins == null || medecins.isEmpty()) {
            System.out.println("No doctors found in the database.");
            showAlert("Information", "Aucun médecin trouvé dans la base de données.", Alert.AlertType.INFORMATION);
            return;
        }

        List<Doctor> doctorData = new ArrayList<>();
        for (Utilisateur medecin : medecins) {
            Doctor doctor = new Doctor(
                    medecin.getId(),
                    medecin.getPrenom(),
                    medecin.getNom(),
                    medecin.getSpeciality(),
                    medecin.getAdresse(),
                    medecin.getImageprofile()
            );
            System.out.println("Doctor Details - " +
                    "Name: " + doctor.getFirstName() + " " + doctor.getLastName() +
                    ", Specialty: " + (doctor.getSpecialty() != null ? doctor.getSpecialty() : "NULL") +
                    ", Address: " + (doctor.getAddress() != null ? doctor.getAddress() : "NULL") +
                    ", ImageProfile: " + (doctor.getImageProfile() != null ? doctor.getImageProfile() : "NULL"));
            doctorData.add(doctor);
        }
        doctors = FXCollections.observableArrayList(doctorData);
        filteredDoctors = new FilteredList<>(doctors, p -> true);
        displayDoctors();
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredDoctors.setPredicate(doctor -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                String fullName = (doctor.getFirstName() + " " + doctor.getLastName()).toLowerCase();
                return fullName.contains(lowerCaseFilter);
            });
            displayDoctors();
        });
    }

    private void setupSpecialtiesFilter() {
        specialtiesFilter.getChildren().clear();
        Set<String> specialties = new HashSet<>();
        for (Doctor doctor : doctors) {
            if (doctor.getSpecialty() != null && !doctor.getSpecialty().isEmpty()) {
                specialties.add(doctor.getSpecialty());
            } else {
                System.out.println("Doctor " + doctor.getFirstName() + " " + doctor.getLastName() + " has no specialty.");
            }
        }
        if (specialties.isEmpty()) {
            Label noSpecialtiesLabel = new Label("Aucune spécialité disponible");
            noSpecialtiesLabel.getStyleClass().add("info-label");
            specialtiesFilter.getChildren().add(noSpecialtiesLabel);
        } else {
            for (String specialty : specialties) {
                CheckBox checkBox = new CheckBox(specialty);
                checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> filterDoctors());
                specialtiesFilter.getChildren().add(checkBox);
            }
        }
    }

    private void setupAddressFilter() {
        addressFilter.getChildren().clear();
        Set<String> addresses = new HashSet<>();
        for (Doctor doctor : doctors) {
            if (doctor.getAddress() != null && !doctor.getAddress().isEmpty()) {
                addresses.add(doctor.getAddress());
            } else {
                System.out.println("Doctor " + doctor.getFirstName() + " " + doctor.getLastName() + " has no address.");
            }
        }
        if (addresses.isEmpty()) {
            Label noAddressesLabel = new Label("Aucune adresse disponible");
            noAddressesLabel.getStyleClass().add("info-label");
            addressFilter.getChildren().add(noAddressesLabel);
        } else {
            for (String address : addresses) {
                CheckBox checkBox = new CheckBox(address);
                checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> filterDoctors());
                addressFilter.getChildren().add(checkBox);
            }
        }
    }

    private void filterDoctors() {
        filteredDoctors.setPredicate(doctor -> {
            boolean specialtyMatch = true;
            boolean addressMatch = true;

            boolean anySpecialtySelected = false;
            for (javafx.scene.Node node : specialtiesFilter.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    if (cb.isSelected()) {
                        anySpecialtySelected = true;
                        if (doctor.getSpecialty() == null || !doctor.getSpecialty().equalsIgnoreCase(cb.getText())) {
                            specialtyMatch = false;
                            break;
                        }
                    }
                }
            }
            if (!anySpecialtySelected) {
                specialtyMatch = true;
            }

            boolean anyAddressSelected = false;
            for (javafx.scene.Node node : addressFilter.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    if (cb.isSelected()) {
                        anyAddressSelected = true;
                        if (doctor.getAddress() == null || !doctor.getAddress().equalsIgnoreCase(cb.getText())) {
                            addressMatch = false;
                            break;
                        }
                    }
                }
            }
            if (!anyAddressSelected) {
                addressMatch = true;
            }

            return specialtyMatch && addressMatch;
        });
        displayDoctors();
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        for (javafx.scene.Node node : specialtiesFilter.getChildren()) {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
            }
        }
        for (javafx.scene.Node node : addressFilter.getChildren()) {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
            }
        }
        filteredDoctors.setPredicate(doctor -> true);
        displayDoctors();
    }

    private void displayDoctors() {
        doctorList.getChildren().clear();
        if (filteredDoctors.isEmpty()) {
            Label noDoctorsLabel = new Label("Aucun médecin trouvé");
            noDoctorsLabel.getStyleClass().add("info-label");
            doctorList.getChildren().add(noDoctorsLabel);
        } else {
            for (Doctor doctor : filteredDoctors) {
                HBox doctorCard = createDoctorCard(doctor);
                doctorList.getChildren().add(doctorCard);
            }
        }
    }

    private HBox createDoctorCard(Doctor doctor) {
        HBox card = new HBox();
        card.getStyleClass().add("doctor-card");
        card.setSpacing(15);
        card.setAlignment(Pos.CENTER_LEFT);

        // Left side: Image and info side by side
        HBox leftBox = new HBox();
        leftBox.setSpacing(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        // Profile image
        ImageView profileImage = new ImageView();
        profileImage.setFitHeight(80); // Increased from 50 to 80
        profileImage.setFitWidth(80);  // Increased from 50 to 80
        profileImage.getStyleClass().add("profile-image"); // Add CSS class for styling
        Image image = loadProfileImage(doctor);
        profileImage.setImage(image);

        // Info box (name, address, specialty)
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(doctor.getFirstName() + " " + doctor.getLastName());
        nameLabel.getStyleClass().add("doctor-name");

        System.out.println("Displaying Doctor: " + doctor.getFirstName() + " " + doctor.getLastName() +
                ", Specialty: " + (doctor.getSpecialty() != null ? doctor.getSpecialty() : "NULL") +
                ", Address: " + (doctor.getAddress() != null ? doctor.getAddress() : "NULL"));

        HBox addressBox = new HBox(5);
        Label addressLabel = new Label("Adresse :");
        addressLabel.getStyleClass().add("info-label");
        String addressDisplay = (doctor.getAddress() != null && !doctor.getAddress().trim().isEmpty()) ? doctor.getAddress() : "Non spécifiée";
        Label addressValue = new Label(addressDisplay);
        addressValue.getStyleClass().add("info-value");
        addressBox.getChildren().addAll(addressLabel, addressValue);

        HBox specialtyBox = new HBox(5);
        Label specialtyLabel = new Label("Spécialité :");
        specialtyLabel.getStyleClass().add("info-label");
        String specialtyDisplay = (doctor.getSpecialty() != null && !doctor.getSpecialty().trim().isEmpty()) ? doctor.getSpecialty() : "Non spécifiée";
        Label specialtyValue = new Label(specialtyDisplay);
        specialtyValue.getStyleClass().add("info-value");
        specialtyBox.getChildren().addAll(specialtyLabel, specialtyValue);

        infoBox.getChildren().addAll(nameLabel, addressBox, specialtyBox);

        leftBox.getChildren().addAll(profileImage, infoBox);

        // Right side: Button aligned to the bottom-right
        VBox rightBox = new VBox();
        rightBox.setAlignment(Pos.BOTTOM_RIGHT);
        rightBox.setPrefWidth(120);
        Button bookButton = new Button("Prendre RDV");
        bookButton.getStyleClass().add("primary-button");
        bookButton.setOnAction(e -> bookAppointment(doctor));
        rightBox.getChildren().add(bookButton);

        // Add both sides to the card
        card.getChildren().addAll(leftBox, rightBox);

        // Ensure the rightBox takes up the remaining space to push the button to the right
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        return card;
    }

    private Image loadProfileImage(Doctor doctor) {
        String imageProfile = doctor.getImageProfile();
        if (imageProfile == null || imageProfile.trim().isEmpty()) {
            System.err.println("No profile image specified for " + doctor.getFirstName() + " " + doctor.getLastName());
            return loadDefaultImage();
        }

        System.out.println("Attempting to load image for " + doctor.getFirstName() + " " + doctor.getLastName() +
                ", ImageProfile: " + imageProfile);

        // Try the profile_images directory
        File imageFile = new File(PROFILE_IMAGES_DIR, imageProfile);
        System.out.println("Checking image in profile_images directory: " + imageFile.getAbsolutePath());

        if (!imageFile.exists()) {
            // Try the working directory as a fallback
            imageFile = new File(imageProfile);
            System.out.println("Checking image in working directory: " + imageFile.getAbsolutePath());
        }

        if (imageFile.exists()) {
            try {
                String fileUrl = imageFile.toURI().toString();
                Image image = new Image(fileUrl);
                if (image.isError()) {
                    throw new IllegalArgumentException("Failed to load image: " + fileUrl);
                }
                return image;
            } catch (Exception e) {
                System.err.println("Error loading profile image for " + doctor.getFirstName() + " " + doctor.getLastName() +
                        ", ImageProfile: " + imageProfile + ", Error: " + e.getMessage());
            }
        } else {
            System.err.println("Image file not found for " + doctor.getFirstName() + " " + doctor.getLastName() +
                    ", ImageProfile: " + imageProfile + ", Path: " + imageFile.getAbsolutePath());
        }

        // Fallback to default image
        return loadDefaultImage();
    }

    private Image loadDefaultImage() {
        try {
            // Load a default image from resources (ensure you have a default image in your resources folder)
            return new Image(getClass().getResourceAsStream(DEFAULT_IMAGE_PATH));
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
            // Return a blank image as a last resort (1x1 transparent pixel)
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
        }
    }

    private void bookAppointment(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Appointment/create_appointment.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the doctor
            Create_appointmentController controller = loader.getController();
            controller.setSelectedDoctor(doctor);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de rendez-vous: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStyleClass().add("custom-alert");
            alert.showAndWait();
        });
    }

    public static class Doctor {
        private int id;
        private String firstName;
        private String lastName;
        private String specialty;
        private String address;
        private String imageProfile;

        public Doctor(int id, String firstName, String lastName, String specialty, String address, String imageProfile) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.specialty = specialty;
            this.address = address;
            this.imageProfile = imageProfile;
        }

        public int getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getSpecialty() { return specialty; }
        public String getAddress() { return address; }
        public String getImageProfile() { return imageProfile; }
    }
}