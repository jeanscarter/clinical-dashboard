package com.cms.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Attachment {

    private Integer id;
    private ClinicalHistory clinicalHistory;
    private Integer clinicalHistoryId;
    private String nombre;
    private String tipo;
    private String rutaArchivo;
    private Long tamanoBytes;
    private LocalDateTime fechaCarga;
    private String descripcion;

    public Attachment() {
        this.fechaCarga = LocalDateTime.now();
    }

    public Attachment(String nombre, String rutaArchivo, String tipo) {
        this();
        this.nombre = nombre;
        this.rutaArchivo = rutaArchivo;
        this.tipo = tipo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ClinicalHistory getClinicalHistory() {
        return clinicalHistory;
    }

    public void setClinicalHistory(ClinicalHistory clinicalHistory) {
        this.clinicalHistory = clinicalHistory;
        this.clinicalHistoryId = clinicalHistory != null ? clinicalHistory.getId() : null;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTamanoFormateado() {
        if (tamanoBytes == null)
            return "0 B";
        if (tamanoBytes < 1024)
            return tamanoBytes + " B";
        if (tamanoBytes < 1024 * 1024)
            return String.format("%.1f KB", tamanoBytes / 1024.0);
        return String.format("%.1f MB", tamanoBytes / (1024.0 * 1024.0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Attachment that = (Attachment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", tamano=" + getTamanoFormateado() +
                '}';
    }
}
