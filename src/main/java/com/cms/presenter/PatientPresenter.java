package com.cms.presenter;

import com.cms.core.validation.PatientValidator;
import com.cms.core.validation.ValidationResult;
import com.cms.domain.Patient;
import com.cms.infra.EventBus;
import com.cms.presenter.events.PatientDeletedEvent;
import com.cms.presenter.events.PatientSavedEvent;
import com.cms.repository.PatientRepository;

import javax.swing.*;
import java.util.List;

public class PatientPresenter implements PatientContract.Presenter {

    private final PatientContract.View view;
    private final PatientRepository patientRepository;
    private final PatientValidator validator;
    private final EventBus eventBus;

    public PatientPresenter(PatientContract.View view, PatientRepository patientRepository) {
        this.view = view;
        this.patientRepository = patientRepository;
        this.validator = new PatientValidator();
        this.eventBus = EventBus.getInstance();
    }

    @Override
    public void loadPatients() {
        view.showLoading(true);
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Patient> doInBackground() {
                return patientRepository.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Patient> patients = get();
                    view.showPatients(patients);
                } catch (Exception e) {
                    view.showError("Error al cargar pacientes: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadPatients();
            return;
        }

        view.showLoading(true);
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Patient> doInBackground() {
                return patientRepository.search(query.trim());
            }

            @Override
            protected void done() {
                try {
                    List<Patient> patients = get();
                    view.showPatients(patients);
                } catch (Exception e) {
                    view.showError("Error al buscar pacientes: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void savePatient(Patient patient) {
        ValidationResult validation = validator.validate(patient);
        if (!validation.isValid()) {
            view.showError(validation.getErrorsAsString());
            return;
        }

        if (patientRepository.existsByCedula(patient.getCedula())) {
            view.showError("Ya existe un paciente con esta cédula");
            return;
        }

        view.showLoading(true);
        SwingWorker<Patient, Void> worker = new SwingWorker<>() {
            @Override
            protected Patient doInBackground() {
                return patientRepository.save(patient);
            }

            @Override
            protected void done() {
                try {
                    Patient saved = get();
                    view.showSuccess("Paciente guardado exitosamente");
                    view.clearForm();
                    eventBus.publish(new PatientSavedEvent(saved));
                    loadPatients();
                } catch (Exception e) {
                    view.showError("Error al guardar paciente: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void updatePatient(Patient patient) {
        ValidationResult validation = validator.validateForUpdate(patient);
        if (!validation.isValid()) {
            view.showError(validation.getErrorsAsString());
            return;
        }

        view.showLoading(true);
        SwingWorker<Patient, Void> worker = new SwingWorker<>() {
            @Override
            protected Patient doInBackground() {
                return patientRepository.save(patient);
            }

            @Override
            protected void done() {
                try {
                    Patient updated = get();
                    view.showSuccess("Paciente actualizado exitosamente");
                    view.clearForm();
                    eventBus.publish(new PatientSavedEvent(updated));
                    loadPatients();
                } catch (Exception e) {
                    view.showError("Error al actualizar paciente: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void deletePatient(Integer patientId) {
        view.showLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                patientRepository.delete(patientId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    view.showSuccess("Paciente eliminado exitosamente");
                    eventBus.publish(new PatientDeletedEvent(patientId));
                    loadPatients();
                } catch (Exception e) {
                    view.showError("Error al eliminar paciente: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void selectPatient(Integer patientId) {
        patientRepository.findById(patientId).ifPresentOrElse(
                view::showPatientDetails,
                () -> view.showError("Paciente no encontrado"));
    }

    @Override
    public void openHistory(Integer patientId) {
        patientRepository.findById(patientId).ifPresentOrElse(
                view::navigateToHistory,
                () -> view.showError("Paciente no encontrado"));
    }
}
