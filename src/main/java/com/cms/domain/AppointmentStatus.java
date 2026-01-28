package com.cms.domain;

/**
 * Enum representing the status of an appointment.
 */
public enum AppointmentStatus {
    PENDING("Pendiente"),
    IN_PROGRESS("En Atención"),
    COMPLETED("Completada"),
    CANCELLED("Cancelada");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AppointmentStatus fromString(String status) {
        if (status == null) {
            return PENDING;
        }
        try {
            return valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
