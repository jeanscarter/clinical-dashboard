package com.cms.service;

import com.cms.core.validation.ClinicalHistoryValidator;
import com.cms.core.validation.ValidationResult;
import com.cms.domain.Attachment;
import com.cms.domain.ClinicalHistory;
import com.cms.repository.AttachmentRepository;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;
import com.cms.service.dto.AttachmentDTO;
import com.cms.service.dto.ClinicalHistoryDTO;
import com.cms.service.exception.NotFoundException;
import com.cms.service.exception.ValidationException;
import com.cms.util.ClipboardImageHandler;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ClinicalHistoryService {

    private final ClinicalHistoryRepository historyRepository;
    private final AttachmentRepository attachmentRepository;
    private final PatientRepository patientRepository;
    private final ClinicalHistoryValidator validator;
    private final ClipboardImageHandler clipboardHandler;

    public ClinicalHistoryService(ClinicalHistoryRepository historyRepository,
            AttachmentRepository attachmentRepository,
            PatientRepository patientRepository) {
        this.historyRepository = historyRepository;
        this.attachmentRepository = attachmentRepository;
        this.patientRepository = patientRepository;
        this.validator = new ClinicalHistoryValidator();
        this.clipboardHandler = new ClipboardImageHandler();
    }

    public ClinicalHistoryDTO createHistory(ClinicalHistoryDTO dto, List<File> attachments) {
        ClinicalHistory history = dto.toEntity();

        ValidationResult validation = validator.validate(history);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }

        if (history.getFechaConsulta() == null) {
            history.setFechaConsulta(LocalDateTime.now());
        }

        ClinicalHistory saved = historyRepository.save(history);

        if (attachments != null && !attachments.isEmpty()) {
            for (File file : attachments) {
                addAttachmentToHistory(saved.getId(), file);
            }
        }

        return enrichDTO(new ClinicalHistoryDTO(saved));
    }

    public ClinicalHistoryDTO updateHistory(Integer id, ClinicalHistoryDTO dto) {
        if (!historyRepository.findById(id).isPresent()) {
            throw new NotFoundException("Historia Clínica", id);
        }

        ClinicalHistory history = dto.toEntity();
        history.setId(id);

        ValidationResult validation = validator.validateForUpdate(history);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }

        ClinicalHistory saved = historyRepository.save(history);
        return enrichDTO(new ClinicalHistoryDTO(saved));
    }

    public void deleteHistory(Integer id) {
        if (!historyRepository.findById(id).isPresent()) {
            throw new NotFoundException("Historia Clínica", id);
        }

        List<Attachment> attachments = attachmentRepository.findByClinicalHistoryId(id);
        for (Attachment attachment : attachments) {
            clipboardHandler.deleteAttachmentFile(attachment.getRutaArchivo());
            attachmentRepository.delete(attachment.getId());
        }

        historyRepository.delete(id);
    }

    public ClinicalHistoryDTO getHistory(Integer id) {
        return historyRepository.findById(id)
                .map(ClinicalHistoryDTO::new)
                .map(this::enrichDTO)
                .orElseThrow(() -> new NotFoundException("Historia Clínica", id));
    }

    public List<ClinicalHistoryDTO> getHistoriesByPatient(Integer patientId) {
        return historyRepository.findByPatientId(patientId).stream()
                .map(ClinicalHistoryDTO::new)
                .map(this::enrichDTO)
                .collect(Collectors.toList());
    }

    public List<ClinicalHistoryDTO> getAllHistories() {
        return historyRepository.findAll().stream()
                .map(ClinicalHistoryDTO::new)
                .map(this::enrichDTO)
                .collect(Collectors.toList());
    }

    public List<ClinicalHistoryDTO> searchHistories(String query, LocalDateTime from, LocalDateTime to) {
        return historyRepository.findAll().stream()
                .filter(h -> matchesSearch(h, query, from, to))
                .map(ClinicalHistoryDTO::new)
                .map(this::enrichDTO)
                .collect(Collectors.toList());
    }

    public long countHistories() {
        return historyRepository.count();
    }

    public long countHistoriesByPatient(Integer patientId) {
        return historyRepository.countByPatientId(patientId);
    }

    private void addAttachmentToHistory(Integer historyId, File file) {
        File destFile = clipboardHandler.copyFileToAttachments(
                file.getAbsolutePath(), historyId).orElse(null);

        if (destFile != null) {
            Attachment attachment = new Attachment();
            attachment.setClinicalHistoryId(historyId);
            attachment.setNombre(file.getName());
            attachment.setRutaArchivo(destFile.getAbsolutePath());
            attachment.setTipo(clipboardHandler.getMimeType(file.getAbsolutePath()));
            attachment.setTamanoBytes(destFile.length());
            attachment.setFechaCarga(LocalDateTime.now());
            attachmentRepository.save(attachment);
        }
    }

    private ClinicalHistoryDTO enrichDTO(ClinicalHistoryDTO dto) {
        if (dto.getPatientId() != null) {
            patientRepository.findById(dto.getPatientId())
                    .ifPresent(p -> dto.setPatientName(p.getNombreCompleto()));
        }

        List<Attachment> attachments = attachmentRepository.findByClinicalHistoryId(dto.getId());
        dto.setAttachments(attachments.stream()
                .map(AttachmentDTO::new)
                .collect(Collectors.toList()));

        return dto;
    }

    private boolean matchesSearch(ClinicalHistory history, String query,
            LocalDateTime from, LocalDateTime to) {
        boolean matchesQuery = true;
        if (query != null && !query.trim().isEmpty()) {
            String lowerQuery = query.toLowerCase();
            matchesQuery = (history.getMotivoConsulta() != null &&
                    history.getMotivoConsulta().toLowerCase().contains(lowerQuery)) ||
                    (history.getDiagnostico() != null &&
                            history.getDiagnostico().toLowerCase().contains(lowerQuery))
                    ||
                    (history.getMedico() != null &&
                            history.getMedico().toLowerCase().contains(lowerQuery));
        }

        boolean matchesDate = true;
        if (from != null && history.getFechaConsulta() != null) {
            matchesDate = !history.getFechaConsulta().isBefore(from);
        }
        if (to != null && history.getFechaConsulta() != null && matchesDate) {
            matchesDate = !history.getFechaConsulta().isAfter(to);
        }

        return matchesQuery && matchesDate;
    }
}
