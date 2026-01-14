package com.cms.service.dto;

import com.cms.domain.Patient;

import java.time.LocalDate;

public class PatientDTO {

    private Integer id;
    private String cedula;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String direccion;
    private String sexo;
    private LocalDate fechaNacimiento;
    private LocalDate fechaRegistro;

    public PatientDTO() {
    }

    public PatientDTO(Patient patient) {
        if (patient != null) {
            this.id = patient.getId();
            this.cedula = patient.getCedula();
            this.nombre = patient.getNombre();
            this.apellido = patient.getApellido();
            this.telefono = patient.getTelefono();
            this.email = patient.getEmail();
            this.direccion = patient.getDireccion();
            this.sexo = patient.getSexo();
            this.fechaNacimiento = patient.getFechaNacimiento();
            this.fechaRegistro = patient.getFechaRegistro();
        }
    }

    public Patient toEntity() {
        Patient patient = new Patient();
        patient.setId(this.id);
        patient.setCedula(this.cedula);
        patient.setNombre(this.nombre);
        patient.setApellido(this.apellido);
        patient.setTelefono(this.telefono);
        patient.setEmail(this.email);
        patient.setDireccion(this.direccion);
        patient.setSexo(this.sexo);
        patient.setFechaNacimiento(this.fechaNacimiento);
        patient.setFechaRegistro(this.fechaRegistro);
        return patient;
    }

    public String getNombreCompleto() {
        if (apellido != null && !apellido.isEmpty()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
