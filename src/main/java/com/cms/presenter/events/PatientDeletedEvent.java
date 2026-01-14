package com.cms.presenter.events;

public class PatientDeletedEvent {
    private final Integer patientId;

    public PatientDeletedEvent(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getPatientId() {
        return patientId;
    }
}
