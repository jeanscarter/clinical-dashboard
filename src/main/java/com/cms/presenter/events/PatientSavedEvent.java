package com.cms.presenter.events;

import com.cms.domain.Patient;

public class PatientSavedEvent {
    private final Patient patient;

    public PatientSavedEvent(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }
}
