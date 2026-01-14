package com.cms.service.dto;

import com.cms.domain.Attachment;

import java.time.LocalDateTime;

public class AttachmentDTO {

    private Integer id;
    private Integer clinicalHistoryId;
    private String nombre;
    private String rutaArchivo;
    private String tipo;
    private Long tamanoBytes;
    private LocalDateTime fechaCreacion;

    public AttachmentDTO() {
    }

    public AttachmentDTO(Attachment attachment) {
        if (attachment != null) {
            this.id = attachment.getId();
            this.clinicalHistoryId = attachment.getClinicalHistoryId();
            this.nombre = attachment.getNombre();
            this.rutaArchivo = attachment.getRutaArchivo();
            this.tipo = attachment.getTipo();
            this.tamanoBytes = attachment.getTamanoBytes();
            this.fechaCreacion = attachment.getFechaCarga();
        }
    }

    public Attachment toEntity() {
        Attachment attachment = new Attachment();
        attachment.setId(this.id);
        attachment.setClinicalHistoryId(this.clinicalHistoryId);
        attachment.setNombre(this.nombre);
        attachment.setRutaArchivo(this.rutaArchivo);
        attachment.setTipo(this.tipo);
        attachment.setTamanoBytes(this.tamanoBytes);
        attachment.setFechaCarga(this.fechaCreacion);
        return attachment;
    }

    public boolean isImage() {
        return tipo != null && tipo.startsWith("image/");
    }

    public String getFormattedSize() {
        if (tamanoBytes == null)
            return "0 B";
        if (tamanoBytes < 1024)
            return tamanoBytes + " B";
        if (tamanoBytes < 1024 * 1024)
            return String.format("%.1f KB", tamanoBytes / 1024.0);
        return String.format("%.1f MB", tamanoBytes / (1024.0 * 1024.0));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClinicalHistoryId() {
        return clinicalHistoryId;
    }

    public void setClinicalHistoryId(Integer clinicalHistoryId) {
        this.clinicalHistoryId = clinicalHistoryId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
