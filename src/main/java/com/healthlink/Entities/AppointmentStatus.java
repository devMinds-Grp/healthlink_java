package com.healthlink.Entities;

public enum AppointmentStatus {  // Add 'public' here
    EN_ATTENTE("en attente"),
    CONFIRME("confirmé"),
    ANNULE("annulé");

    private final String value;

    AppointmentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AppointmentStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Statut invalide: le statut ne peut pas être null");
        }
        String normalizedStatus = status.toLowerCase().trim();
        for (AppointmentStatus s : values()) {
            if (s.getValue().equals(normalizedStatus) || normalizedStatus.equals(s.name().toLowerCase())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Statut invalide: " + status);
    }
}