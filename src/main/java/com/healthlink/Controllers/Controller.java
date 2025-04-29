package com.healthlink.Controllers;

import com.healthlink.Entites.Hospital;
import com.healthlink.Entites.DistanceCalculator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.json.JSONArray; // Import for JSONArray
import org.json.JSONObject; // Import for JSONObject

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML private Label positionLabel;
    @FXML private TextField maxDistanceField;
    @FXML private ListView<String> hospitalList;
    private double userLat;
    private double userLon;

    @FXML
    public void initialize() {
        fetchUserLocation();
    }

    private void fetchUserLocation() {
        new Thread(() -> {
            try {
                // Use ip-api.com to get user location
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://ip-api.com/json"))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject json = new JSONObject(response.body());
                if ("success".equals(json.getString("status"))) {
                    userLat = json.getDouble("lat");
                    userLon = json.getDouble("lon");
                    Platform.runLater(() -> {
                        positionLabel.setText(String.format("Position: %.4f, %.4f", userLat, userLon));
                        findHospitals(); // Auto-trigger hospital search
                    });
                } else {
                    throw new Exception("Geolocation failed: " + json.getString("message"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    positionLabel.setText("Geolocation error: " + e.getMessage());
                    hospitalList.getItems().clear();
                    hospitalList.getItems().add("Unable to detect position");
                });
            }
        }).start();
    }

    @FXML
    public void retryLocation() {
        positionLabel.setText("Detecting position...");
        hospitalList.getItems().clear();
        fetchUserLocation();
    }

    @FXML
    public void findHospitals() {
        try {
            // Check if position is detected
            if (userLat == 0.0 && userLon == 0.0) {
                hospitalList.getItems().clear();
                hospitalList.getItems().add("Error: Position not detected");
                return;
            }

            double maxDistance = maxDistanceField.getText().isEmpty() ?
                    10.0 : Double.parseDouble(maxDistanceField.getText());

            // Fetch hospitals from Geoapify
            List<Hospital> hospitals = fetchHospitals(userLat, userLon);

            // Filter and sort hospitals by distance
            ObservableList<String> hospitalItems = FXCollections.observableArrayList();
            for (Hospital hospital : hospitals) {
                double distance = DistanceCalculator.calculateDistance(
                        userLat, userLon, hospital.getLatitude(), hospital.getLongitude());
                if (distance <= maxDistance) {
                    hospitalItems.add(String.format("%s: %.2f km", hospital.getName(), distance));
                }
            }
            hospitalItems.sort((a, b) -> {
                double distA = Double.parseDouble(a.split(": ")[1].replace(" km", ""));
                double distB = Double.parseDouble(b.split(": ")[1].replace(" km", ""));
                return Double.compare(distA, distB);
            });
            hospitalList.setItems(hospitalItems);

            if (hospitalItems.isEmpty()) {
                hospitalList.getItems().add("No hospitals found within " + maxDistance + " km");
            }
        } catch (NumberFormatException e) {
            hospitalList.getItems().clear();
            hospitalList.getItems().add("Invalid max distance");
        } catch (Exception e) {
            hospitalList.getItems().clear();
            hospitalList.getItems().add("Error fetching hospitals: " + e.getMessage());
        }
    }

    @FXML
    public void clearFields() {
        maxDistanceField.clear();
        hospitalList.getItems().clear();
        fetchUserLocation();
    }

    private List<Hospital> fetchHospitals(double lat, double lon) throws Exception {
        String apiKey = "281ae7e98c5f453a877261ce339decbc"; // Replace with your Geoapify API key
        String urlString = String.format(
                "https://api.geoapify.com/v2/places?categories=healthcare.hospital&filter=circle:%f,%f,100000&limit=20&apiKey=%s",
                lon, lat, apiKey
        );
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Geoapify Response: " + response.body()); // Debug

        List<Hospital> hospitals = new ArrayList<>();
        JSONObject json = new JSONObject(response.body());
        JSONArray features = json.getJSONArray("features");

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");
            JSONObject geometry = feature.getJSONObject("geometry");
            JSONArray coordinates = geometry.getJSONArray("coordinates");

            String name = properties.has("name") ? properties.getString("name") : "Hospital " + (i + 1);
            double hospitalLon = coordinates.getDouble(0);
            double hospitalLat = coordinates.getDouble(1);

            hospitals.add(new Hospital(name, hospitalLat, hospitalLon));
        }

        return hospitals;
    }
}