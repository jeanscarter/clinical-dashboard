package com.cms.service.dto;

import com.cms.domain.ClinicalHistory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClinicalHistoryDTO {

    private Integer id;
    private Integer patientId;
    private String patientName;
    private LocalDateTime fechaConsulta;
    private String motivoConsulta;
    private String antecedentes;
    private String examenFisico;
    private String diagnostico;
    private String conducta;
    private String observaciones;
    private String medico;
    private List<AttachmentDTO> attachments;

    public ClinicalHistoryDTO() {
        this.attachments = new ArrayList<>();
    }

    public ClinicalHistoryDTO(ClinicalHistory history) {
        this();
        if (history != null) {
            this.id = history.getId();
            this.patientId = history.getPatientId();
            this.fechaConsulta = history.getFechaConsulta();
            this.motivoConsulta = history.getMotivoConsulta();
            this.antecedentes = history.getAntecedentes();
            this.examenFisico = history.getExamenFisico();
            this.diagnostico = history.getDiagnostico();
            this.conducta = history.getConducta();
            this.observaciones = history.getObservaciones();
            this.medico = history.getMedico();
        }
    }

    public ClinicalHistory toEntity() {
        ClinicalHistory history = new ClinicalHistory();
        history.setId(this.id);
        history.setPatientId(this.patientId);
        history.setFechaConsulta(this.fechaConsulta);
        history.setMotivoConsulta(this.motivoConsulta);
        history.setAntecedentes(this.antecedentes);
        history.setExamenFisico(this.examenFisico);
        history.setDiagnostico(this.diagnostico);
        history.setConducta(this.conducta);
        history.setObservaciones(this.observaciones);
        history.setMedico(this.medico);
        return history;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDateTime getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDateTime fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getExamenFisico() {
        return examenFisico;
    }

    public void setExamenFisico(String examenFisico) {
        this.examenFisico = examenFisico;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getConducta() {
        return conducta;
    }

    public void setConducta(String conducta) {
        this.conducta = conducta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public List<AttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDTO> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    public void addAttachment(AttachmentDTO attachment) {
        this.attachments.add(attachment);
    }
}
