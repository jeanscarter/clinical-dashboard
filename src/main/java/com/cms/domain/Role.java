package com.cms.domain;

public enum Role {
    ADMIN("Administrador"),
    DOCTOR("Doctor"),
    RECEPCIONISTA("Recepcionista");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canManagePatients() {
        return true;
    }

    public boolean canViewHistories() {
        return true;
    }

    public boolean canEditHistories() {
        return this == ADMIN || this == DOCTOR;
    }

    public boolean canDeleteHistories() {
        return this == ADMIN || this == DOCTOR;
    }

    public boolean canManageUsers() {
        return this == ADMIN;
    }

    public boolean canAccessSettings() {
        return this == ADMIN;
    }

    public boolean canGenerateReports() {
        return this == ADMIN || this == DOCTOR;
    }

    public boolean canManageBackups() {
        return this == ADMIN;
    }
}
