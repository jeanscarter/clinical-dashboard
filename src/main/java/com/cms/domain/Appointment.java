package com.cms.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Entity representing a scheduled appointment.
 */
public class Appointment {

    private Integer id;
    private Integer patientId;
    private Integer doctorId;
    private LocalDate date;
    private LocalTime time;
    private String reason;
    private AppointmentStatus status;
    private LocalDateTime createdAt;

    // Transient fields for display
    private Patient patient;
    private User doctor;

    public Appointment() {
        this.status = AppointmentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Appointment(Integer patientId, LocalDate date, LocalTime time, String reason) {
        this();
        this.patientId = patientId;
        this.date = date;
        this.time = time;
        this.reason = reason;
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

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getId();
        }
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.doctorId = doctor.getId();
        }
    }

    /**
     * Returns the appointment date and time combined.
     */
    public LocalDateTime getDateTime() {
        if (date == null || time == null) {
            return null;
        }
        return LocalDateTime.of(date, time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", date=" + date +
                ", time=" + time +
                ", status=" + status +
                '}';
    }
}
