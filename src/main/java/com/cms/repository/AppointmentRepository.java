package com.cms.repository;

import com.cms.core.Repository;
import com.cms.domain.Appointment;
import com.cms.domain.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Appointment entity.
 */
public interface AppointmentRepository extends Repository<Appointment, Integer> {

    /**
     * Find all appointments for a specific date.
     */
    List<Appointment> findByDate(LocalDate date);

    /**
     * Find all appointments for a specific patient.
     */
    List<Appointment> findByPatientId(Integer patientId);

    /**
     * Find all appointments with a specific status.
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Find all appointments for today.
     */
    List<Appointment> findTodayAppointments();

    /**
     * Find all appointments for a date range.
     */
    List<Appointment> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Count appointments for a specific date.
     */
    long countByDate(LocalDate date);

    /**
     * Count all appointments.
     */
    long count();
}
