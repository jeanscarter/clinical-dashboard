package com.cms.core.validation;

import com.cms.domain.ClinicalHistory;

public class ClinicalHistoryValidator {

    public ValidationResult validate(ClinicalHistory history) {
        ValidationResult result = new ValidationResult();

        if (history.getPatientId() == null) {
            result.addError("Debe seleccionar un paciente");
        }

        if (!ValidationResult.isNotEmpty(history.getMotivoConsulta())) {
            result.addError("El motivo de consulta es obligatorio");
        }

        if (history.getFechaConsulta() == null) {
            result.addError("La fecha de consulta es obligatoria");
        }

        return result;
    }

    public ValidationResult validateForUpdate(ClinicalHistory history) {
        ValidationResult result = validate(history);

        if (history.getId() == null) {
            result.addError("La historia clínica debe tener un ID para actualizar");
        }

        return result;
    }
}
