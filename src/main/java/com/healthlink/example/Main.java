package com.healthlink.example;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entities.Care;
import com.healthlink.Entities.CareResponse;
import com.healthlink.Services.CareResponseService;
import com.healthlink.Services.CareService;
import com.healthlink.utils.MyDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static CareService careService;
    private static CareResponseService careResponseService;

    public static void main(String[] args) throws SQLException {
        Connection connection = MyDB.getInstance().getConnection();
        careService = new CareService(connection);
        careResponseService = new CareResponseService(connection);

        boolean running = true;
        while (running) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Manage Cares");
            System.out.println("2. Manage Care Responses");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> showCareMenu();
                case 2 -> showResponseMenu();
                case 3 -> running = false;
                default -> System.out.println("Invalid option!");
            }
        }
        System.out.println("Exiting...");
    }

    // ==================== CARE METHODS ====================
    private static void showCareMenu() {
        System.out.println("\n=== Care Menu ===");
        System.out.println("1. List all Cares");
        System.out.println("2. Add new Care");
        System.out.println("3. Edit Care");
        System.out.println("4. Delete Care");
        System.out.println("5. View Care with Responses");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> listAllCares();
            case 2 -> addCare();
            case 3 -> editCare();
            case 4 -> deleteCare();
            case 5 -> viewCareWithResponses();
            default -> System.out.println("Invalid option!");
        }
    }

    private static void listAllCares() {
        List<Care> cares = careService.getAllCares();
        if (cares.isEmpty()) {
            System.out.println("No cares found.");
            return;
        }
        System.out.println("\nAll Cares:");
        cares.forEach(care -> System.out.printf(
                "ID: %d | Date: %s | Address: %s | Description: %s | Patient ID: %s\n",
                care.getId(),
                care.getDate(),
                care.getAddress(),
                care.getDescription(),
                care.getPatient() != null ? care.getPatient().getId() : "null"
        ));
    }

    private static void addCare() {
        System.out.println("\n=== Add New Care ===");
        Care care = new Care();

        System.out.print("Enter address: ");
        care.setAddress(scanner.nextLine());

        System.out.print("Enter description: ");
        care.setDescription(scanner.nextLine());

        // Optionally set a patient ID
        System.out.print("Enter Patient ID (or leave blank for no patient): ");
        String patientIdInput = scanner.nextLine();
        if (!patientIdInput.isEmpty()) {
            int patientId = Integer.parseInt(patientIdInput);
            Utilisateur patient = new Utilisateur();
            patient.setId(patientId);
            care.setPatient(patient);
        }

        if (careService.addCare(care)) {
            System.out.println("✅ Care added successfully! ID: " + care.getId());
        } else {
            System.out.println("❌ Failed to add care.");
        }
    }

    private static void editCare() {
        listAllCares();
        System.out.print("\nEnter Care ID to edit: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Care care = findCareById(id);
        if (care == null) return;

        System.out.print("Enter new address (current: " + care.getAddress() + "): ");
        String address = scanner.nextLine();
        if (!address.isEmpty()) care.setAddress(address);

        System.out.print("Enter new description (current: " + care.getDescription() + "): ");
        String description = scanner.nextLine();
        if (!description.isEmpty()) care.setDescription(description);

        if (careService.updateCare(care)) {
            System.out.println("✅ Care updated!");
        } else {
            System.out.println("❌ Failed to update care.");
        }
    }

    private static void deleteCare() {
        listAllCares();
        System.out.print("\nEnter Care ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        // Use the overloaded method without user permission checks
        if (careService.deleteCare(id)) {
            System.out.println("✅ Care and all its responses deleted!");
        } else {
            System.out.println("❌ Failed to delete care.");
        }
    }

    private static void viewCareWithResponses() {
        listAllCares();
        System.out.print("\nEnter Care ID to view: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Care care = findCareById(id);
        if (care == null) return;

        System.out.println("\nCare Details:");
        System.out.printf("ID: %d | Date: %s\nAddress: %s\nDescription: %s\nPatient ID: %s\n",
                care.getId(),
                care.getDate(),
                care.getAddress(),
                care.getDescription(),
                care.getPatient() != null ? care.getPatient().getId() : "null");

        System.out.println("\nResponses:");
        if (care.getResponses() == null || care.getResponses().isEmpty()) {
            System.out.println("No responses yet.");
        } else {
            care.getResponses().forEach(r ->
                    System.out.printf("ID: %d | Date: %s | Content: %s\n",
                            r.getId(), r.getDateRep(), r.getContenuRep()));
        }
    }

    private static Care findCareById(int id) {
        Care care = careService.getAllCares().stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);

        if (care == null) {
            System.out.println("Care not found!");
        }
        return care;
    }

    // ==================== RESPONSE METHODS ====================
    private static void showResponseMenu() {
        System.out.println("\n=== Response Menu ===");
        System.out.println("1. List responses for Care");
        System.out.println("2. Add new Response");
        System.out.println("3. Edit Response");
        System.out.println("4. Delete Response");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> listResponsesForCare();
            case 2 -> addResponse();
            case 3 -> editResponse();
            case 4 -> deleteResponse();
            default -> System.out.println("Invalid option!");
        }
    }

    private static void listResponsesForCare() {
        listAllCares();
        System.out.print("\nEnter Care ID to list responses: ");
        int careId = scanner.nextInt();
        scanner.nextLine();

        Care care = findCareById(careId);
        if (care == null) return;

        System.out.println("\nResponses:");
        if (care.getResponses() == null || care.getResponses().isEmpty()) {
            System.out.println("No responses found for this care.");
        } else {
            care.getResponses().forEach(r ->
                    System.out.printf("ID: %d | Date: %s | Content: %s\n",
                            r.getId(), r.getDateRep(), r.getContenuRep()));
        }
    }

    private static void addResponse() {
        try {
            listAllCares();
            System.out.print("\nEnter Care ID to add response: ");
            int careId = scanner.nextInt();
            scanner.nextLine();

            // Verify care exists
            if (!careService.careExists(careId)) {
                System.out.println("Error: Care ID " + careId + " doesn't exist");
                return;
            }

            CareResponse response = new CareResponse();
            System.out.print("Enter response content: ");
            String content = scanner.nextLine();

            if (content == null || content.trim().isEmpty()) {
                System.out.println("Error: Response content cannot be empty");
                return;
            }

            response.setContenuRep(content);

            // Debug output
            System.out.println("Attempting to add response with:");
            System.out.println("Content: " + response.getContenuRep());
            System.out.println("Date: " + response.getDateRep());
            System.out.println("Care ID: " + careId);

            // Add to database
            boolean dbSuccess = careResponseService.addCareResponse(response, careId);

            if (dbSuccess) {
                // Update in-memory Care object
                Care care = careService.getAllCares().stream()
                        .filter(c -> c.getId() == careId)
                        .findFirst()
                        .orElse(null);

                if (care != null) {
                    care.addResponse(response);
                    System.out.println("✅ Response added! ID: " + response.getId());
                } else {
                    System.out.println("⚠️ Response added to DB but couldn't update local care object");
                }
            } else {
                System.out.println("❌ Database operation failed - check server logs");
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in addResponse: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void editResponse() {
        listAllCares();
        System.out.print("\nEnter Care ID to edit responses: ");
        int careId = scanner.nextInt();
        scanner.nextLine();

        Care care = findCareById(careId);
        if (care == null || care.getResponses() == null || care.getResponses().isEmpty()) {
            System.out.println("No responses found.");
            return;
        }

        care.getResponses().forEach(r ->
                System.out.printf("ID: %d | Content: %s\n", r.getId(), r.getContenuRep()));

        System.out.print("Enter Response ID to edit: ");
        int responseId = scanner.nextInt();
        scanner.nextLine();

        CareResponse response = care.getResponses().stream()
                .filter(r -> r.getId() == responseId)
                .findFirst()
                .orElse(null);

        if (response == null) {
            System.out.println("Response not found!");
            return;
        }

        System.out.print("Enter new content: ");
        response.setContenuRep(scanner.nextLine());

        if (careResponseService.updateCareResponse(response)) {
            System.out.println("✅ Response updated!");
        } else {
            System.out.println("❌ Failed to update response.");
        }
    }

    private static void deleteResponse() {
        listAllCares();
        System.out.print("\nEnter Care ID to delete responses: ");
        int careId = scanner.nextInt();
        scanner.nextLine();

        Care care = findCareById(careId);
        if (care == null || care.getResponses() == null || care.getResponses().isEmpty()) {
            System.out.println("No responses found.");
            return;
        }

        care.getResponses().forEach(r ->
                System.out.printf("ID: %d | Content: %s\n", r.getId(), r.getContenuRep()));

        System.out.print("Enter Response ID to delete: ");
        int responseId = scanner.nextInt();
        scanner.nextLine();

        if (careResponseService.deleteCareResponse(responseId)) {
            care.getResponses().removeIf(r -> r.getId() == responseId);
            System.out.println("✅ Response deleted!");
        } else {
            System.out.println("❌ Failed to delete response.");
        }
    }
}