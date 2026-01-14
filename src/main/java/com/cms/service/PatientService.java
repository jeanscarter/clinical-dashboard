package com.cms.service;

import com.cms.core.validation.PatientValidator;
import com.cms.core.validation.ValidationResult;
import com.cms.domain.Patient;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;
import com.cms.service.dto.PatientDTO;
import com.cms.service.exception.BusinessException;
import com.cms.service.exception.NotFoundException;
import com.cms.service.exception.ValidationException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PatientService {

    private final PatientRepository patientRepository;
    private final ClinicalHistoryRepository historyRepository;
    private final PatientValidator validator;

    public PatientService(PatientRepository patientRepository,
            ClinicalHistoryRepository historyRepository) {
        this.patientRepository = patientRepository;
        this.historyRepository = historyRepository;
        this.validator = new PatientValidator();
    }

    public PatientDTO createPatient(PatientDTO dto) {
        Patient patient = dto.toEntity();

        ValidationResult validation = validator.validate(patient);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }

        if (existsByCedula(dto.getCedula(), null)) {
            throw new BusinessException("DUPLICATE_CEDULA",
                    "Ya existe un paciente con la cédula " + dto.getCedula());
        }

        patient.setFechaRegistro(LocalDate.now());
        Patient saved = patientRepository.save(patient);

        return new PatientDTO(saved);
    }

    public PatientDTO updatePatient(Integer id, PatientDTO dto) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paciente", id));

        Patient patient = dto.toEntity();
        patient.setId(id);
        patient.setFechaRegistro(existing.getFechaRegistro());

        ValidationResult validation = validator.validateForUpdate(patient);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }

        if (existsByCedula(dto.getCedula(), id)) {
            throw new BusinessException("DUPLICATE_CEDULA",
                    "Ya existe otro paciente con la cédula " + dto.getCedula());
        }

        Patient saved = patientRepository.save(patient);
        return new PatientDTO(saved);
    }

    public void deletePatient(Integer id) {
        if (!patientRepository.findById(id).isPresent()) {
            throw new NotFoundException("Paciente", id);
        }

        long historyCount = historyRepository.countByPatientId(id);
        if (historyCount > 0) {
            throw new BusinessException("HAS_HISTORIES",
                    "No se puede eliminar el paciente porque tiene " + historyCount +
                            " historial(es) clínico(s) asociado(s)");
        }

        patientRepository.delete(id);
    }

    public PatientDTO getPatient(Integer id) {
        return patientRepository.findById(id)
                .map(PatientDTO::new)
                .orElseThrow(() -> new NotFoundException("Paciente", id));
    }

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(PatientDTO::new)
                .collect(Collectors.toList());
    }

    public List<PatientDTO> searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientRepository.search(query).stream()
                .map(PatientDTO::new)
                .collect(Collectors.toList());
    }

    public boolean existsByCedula(String cedula, Integer excludeId) {
        return patientRepository.findAll().stream()
                .anyMatch(p -> p.getCedula().equalsIgnoreCase(cedula) &&
                        (excludeId == null || !p.getId().equals(excludeId)));
    }

    public long countPatients() {
        return patientRepository.count();
    }
}
