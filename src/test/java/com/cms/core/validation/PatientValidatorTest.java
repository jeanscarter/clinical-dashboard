package com.cms.core.validation;

import com.cms.domain.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatientValidatorTest {

    private PatientValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PatientValidator();
    }

    @Test
    void validate_shouldReturnValid_whenPatientHasRequiredFields() {
        Patient patient = new Patient();
        patient.setCedula("123456789");
        patient.setNombre("Juan");
        patient.setApellido("Pérez");

        ValidationResult result = validator.validate(patient);

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_shouldReturnInvalid_whenCedulaIsEmpty() {
        Patient patient = new Patient();
        patient.setCedula("");
        patient.setNombre("Juan");

        ValidationResult result = validator.validate(patient);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.toLowerCase().contains("cédula")));
    }

    @Test
    void validate_shouldReturnInvalid_whenNombreIsEmpty() {
        Patient patient = new Patient();
        patient.setCedula("123456789");
        patient.setNombre("");

        ValidationResult result = validator.validate(patient);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.toLowerCase().contains("nombre")));
    }

    @Test
    void validate_shouldReturnInvalid_whenBothFieldsEmpty() {
        Patient patient = new Patient();
        patient.setCedula("");
        patient.setNombre("");

        ValidationResult result = validator.validate(patient);

        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    void validate_shouldReturnInvalid_whenPatientIsNull() {
        ValidationResult result = validator.validate(null);

        assertFalse(result.isValid());
    }

    @Test
    void validate_shouldAcceptValidEmail() {
        Patient patient = new Patient();
        patient.setCedula("123456789");
        patient.setNombre("Juan");
        patient.setEmail("juan@example.com");

        ValidationResult result = validator.validate(patient);

        assertTrue(result.isValid());
    }

    @Test
    void validate_shouldAcceptEmptyEmail() {
        Patient patient = new Patient();
        patient.setCedula("123456789");
        patient.setNombre("Juan");
        patient.setEmail("");

        ValidationResult result = validator.validate(patient);

        assertTrue(result.isValid());
    }
}
