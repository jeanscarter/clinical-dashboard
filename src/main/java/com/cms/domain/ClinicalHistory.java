package com.cms.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClinicalHistory {

    private Integer id;
    private Patient patient;
    private Integer patientId;
    private LocalDateTime fechaConsulta;
    private String motivoConsulta;
    private String antecedentes;
    private String examenFisico;
    private String diagnostico;
    private String conducta;
    private String observaciones;
    private String medico;
    private List<Attachment> adjuntos;

    public ClinicalHistory() {
        this.adjuntos = new ArrayList<>();
        this.fechaConsulta = LocalDateTime.now();
    }

    public ClinicalHistory(Patient patient, String motivoConsulta) {
        this();
        this.patient = patient;
        this.patientId = patient != null ? patient.getId() : null;
        this.motivoConsulta = motivoConsulta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        this.patientId = patient != null ? patient.getId() : null;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
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

    public List<Attachment> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(List<Attachment> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public void addAdjunto(Attachment adjunto) {
        this.adjuntos.add(adjunto);
        adjunto.setClinicalHistory(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClinicalHistory that = (ClinicalHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClinicalHistory{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", fechaConsulta=" + fechaConsulta +
                ", diagnostico='" + diagnostico + '\'' +
                '}';
    }
}
