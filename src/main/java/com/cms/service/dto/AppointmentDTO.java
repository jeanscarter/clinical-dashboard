package com.cms.service.dto;

import com.cms.domain.Appointment;
import com.cms.domain.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object for Appointment entity.
 */
public class AppointmentDTO {

    private Integer id;
    private Integer patientId;
    private String patientName;
    private Integer patientAge;
    private Integer doctorId;
    private String doctorName;
    private LocalDate date;
    private LocalTime time;
    private String reason;
    private AppointmentStatus status;
    private LocalDateTime createdAt;

    public AppointmentDTO() {
    }

    public AppointmentDTO(Appointment appointment) {
        this.id = appointment.getId();
        this.patientId = appointment.getPatientId();
        this.doctorId = appointment.getDoctorId();
        this.date = appointment.getDate();
        this.time = appointment.getTime();
        this.reason = appointment.getReason();
        this.status = appointment.getStatus();
        this.createdAt = appointment.getCreatedAt();

        if (appointment.getPatient() != null) {
            this.patientName = appointment.getPatient().getNombreCompleto();
            this.patientAge = appointment.getPatient().getAge();
        }
        if (appointment.getDoctor() != null) {
            this.doctorName = appointment.getDoctor().getFullName();
        }
    }

    public Appointment toEntity() {
        Appointment appointment = new Appointment();
        appointment.setId(this.id);
        appointment.setPatientId(this.patientId);
        appointment.setDoctorId(this.doctorId);
        appointment.setDate(this.date);
        appointment.setTime(this.time);
        appointment.setReason(this.reason);
        appointment.setStatus(this.status != null ? this.status : AppointmentStatus.PENDING);
        appointment.setCreatedAt(this.createdAt);
        return appointment;
    }

    // Getters and Setters

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

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns formatted time string for display.
     */
    public String getFormattedTime() {
        return time != null ? String.format("%02d:%02d", time.getHour(), time.getMinute()) : "";
    }

    /**
     * Returns status display name.
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }
}
