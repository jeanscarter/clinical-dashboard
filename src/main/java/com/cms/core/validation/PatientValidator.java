package com.cms.core.validation;

import com.cms.domain.Patient;

public class PatientValidator {

    public ValidationResult validate(Patient patient) {
        ValidationResult result = new ValidationResult();

        if (patient == null) {
            result.addError("El paciente no puede ser nulo");
            return result;
        }

        if (!ValidationResult.isNotEmpty(patient.getCedula())) {
            result.addError("La cédula es obligatoria");
        } else if (!ValidationResult.isValidCedula(patient.getCedula())) {
            result.addError("Formato de cédula inválido (ej: V-12345678)");
        }

        if (!ValidationResult.isNotEmpty(patient.getNombre())) {
            result.addError("El nombre es obligatorio");
        }

        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) {
            if (!ValidationResult.isValidEmail(patient.getEmail())) {
                result.addError("Formato de email inválido");
            }
        }

        if (patient.getTelefono() != null && !patient.getTelefono().trim().isEmpty()) {
            if (!ValidationResult.isValidPhone(patient.getTelefono())) {
                result.addError("Formato de teléfono inválido");
            }
        }

        return result;
    }

    public ValidationResult validateForUpdate(Patient patient) {
        ValidationResult result = validate(patient);

        if (patient.getId() == null) {
            result.addError("El paciente debe tener un ID para actualizar");
        }

        return result;
    }
}
