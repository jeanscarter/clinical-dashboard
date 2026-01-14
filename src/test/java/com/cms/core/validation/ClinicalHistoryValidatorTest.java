package com.cms.core.validation;

import com.cms.domain.ClinicalHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClinicalHistoryValidatorTest {

    private ClinicalHistoryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClinicalHistoryValidator();
    }

    @Test
    void validate_shouldReturnValid_whenHistoryHasRequiredFields() {
        ClinicalHistory history = new ClinicalHistory();
        history.setPatientId(1);
        history.setFechaConsulta(LocalDateTime.now());
        history.setMotivoConsulta("Consulta de rutina");

        ValidationResult result = validator.validate(history);

        assertTrue(result.isValid());
    }

    @Test
    void validate_shouldReturnInvalid_whenPatientIdIsNull() {
        ClinicalHistory history = new ClinicalHistory();
        history.setPatientId(null);
        history.setFechaConsulta(LocalDateTime.now());
        history.setMotivoConsulta("Consulta de rutina");

        ValidationResult result = validator.validate(history);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.toLowerCase().contains("paciente")));
    }

    @Test
    void validate_shouldReturnInvalid_whenMotivoConsultaIsEmpty() {
        ClinicalHistory history = new ClinicalHistory();
        history.setPatientId(1);
        history.setFechaConsulta(LocalDateTime.now());
        history.setMotivoConsulta("");

        ValidationResult result = validator.validate(history);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.toLowerCase().contains("motivo")));
    }

    @Test
    void validate_shouldReturnInvalid_whenFechaConsultaIsNull() {
        ClinicalHistory history = new ClinicalHistory();
        history.setPatientId(1);
        history.setFechaConsulta(null);
        history.setMotivoConsulta("Consulta de rutina");

        ValidationResult result = validator.validate(history);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.toLowerCase().contains("fecha")));
    }

    @Test
    void validate_shouldReturnInvalid_whenHistoryIsNull() {
        ValidationResult result = validator.validate(null);

        assertFalse(result.isValid());
    }

    @Test
    void validate_shouldReturnMultipleErrors_whenMultipleFieldsInvalid() {
        ClinicalHistory history = new ClinicalHistory();
        history.setPatientId(null);
        history.setFechaConsulta(null);
        history.setMotivoConsulta("");

        ValidationResult result = validator.validate(history);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 3);
    }
}
