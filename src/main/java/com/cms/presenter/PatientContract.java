package com.cms.presenter;

import com.cms.domain.Patient;

import java.util.List;

public interface PatientContract {

    interface View {
        void showPatients(List<Patient> patients);

        void showPatientDetails(Patient patient);

        void showError(String message);

        void showSuccess(String message);

        void showLoading(boolean loading);

        void clearForm();

        void navigateToHistory(Patient patient);
    }

    interface Presenter {
        void loadPatients();

        void searchPatients(String query);

        void savePatient(Patient patient);

        void updatePatient(Patient patient);

        void deletePatient(Integer patientId);

        void selectPatient(Integer patientId);

        void openHistory(Integer patientId);
    }
}
