package com.cms.service;

import com.cms.domain.Appointment;
import com.cms.domain.AppointmentStatus;
import com.cms.repository.AppointmentRepository;
import com.cms.repository.PatientRepository;
import com.cms.repository.UserRepository;
import com.cms.service.dto.AppointmentDTO;
import com.cms.service.exception.NotFoundException;
import com.cms.service.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing appointments (agenda).
 */
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    // Current logged-in doctor ID (set by authentication)
    private Integer currentDoctorId;

    public AppointmentService(AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    /**
     * Sets the current logged-in doctor ID.
     */
    public void setCurrentDoctorId(Integer doctorId) {
        this.currentDoctorId = doctorId;
    }

    /**
     * Creates a new appointment with the auto-assigned current doctor.
     */
    public AppointmentDTO createAppointment(AppointmentDTO dto) {
        validateAppointment(dto);

        Appointment appointment = dto.toEntity();

        // Auto-assign current doctor if not specified
        if (appointment.getDoctorId() == null && currentDoctorId != null) {
            appointment.setDoctorId(currentDoctorId);
        }

        Appointment saved = appointmentRepository.save(appointment);
        return enrichDTO(new AppointmentDTO(saved));
    }

    /**
     * Creates a walk-in appointment (immediate, for today, now).
     * Useful for patients arriving without prior appointment.
     */
    public AppointmentDTO createWalkInAppointment(Integer patientId, String reason) {
        if (patientId == null) {
            throw new ValidationException("Patient ID is required");
        }

        if (!patientRepository.findById(patientId).isPresent()) {
            throw new NotFoundException("Paciente", patientId);
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDate(LocalDate.now());
        appointment.setTime(LocalTime.now());
        appointment.setReason(reason != null ? reason : "Consulta espontánea");
        appointment.setStatus(AppointmentStatus.PENDING);

        if (currentDoctorId != null) {
            appointment.setDoctorId(currentDoctorId);
        }

        Appointment saved = appointmentRepository.save(appointment);
        return enrichDTO(new AppointmentDTO(saved));
    }

    /**
     * Gets all appointments for today, sorted by time.
     */
    public List<AppointmentDTO> getTodayAppointments() {
        return appointmentRepository.findTodayAppointments().stream()
                .map(AppointmentDTO::new)
                .map(this::enrichDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets all appointments for a specific date.
     */
    public List<AppointmentDTO> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByDate(date).stream()
                .map(AppointmentDTO::new)
                .map(this::enrichDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets all appointments for a patient.
     */
    public List<AppointmentDTO> getAppointmentsByPatient(Integer patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(AppointmentDTO::new)
                .map(this::enrichDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of an appointment.
     */
    public AppointmentDTO updateStatus(Integer appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Cita", appointmentId));

        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);
        return enrichDTO(new AppointmentDTO(saved));
    }

    /**
     * Marks an appointment as "in progress" (patient is being attended).
     */
    public AppointmentDTO startAppointment(Integer appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.IN_PROGRESS);
    }

    /**
     * Marks an appointment as completed.
     */
    public AppointmentDTO completeAppointment(Integer appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.COMPLETED);
    }

    /**
     * Cancels an appointment.
     */
    public AppointmentDTO cancelAppointment(Integer appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    /**
     * Gets the next pending appointment for today.
     */
    public AppointmentDTO getNextPendingAppointment() {
        List<Appointment> todayAppts = appointmentRepository.findTodayAppointments();
        return todayAppts.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING)
                .findFirst()
                .map(AppointmentDTO::new)
                .map(this::enrichDTO)
                .orElse(null);
    }

    /**
     * Deletes an appointment.
     */
    public void deleteAppointment(Integer appointmentId) {
        if (!appointmentRepository.findById(appointmentId).isPresent()) {
            throw new NotFoundException("Cita", appointmentId);
        }
        appointmentRepository.delete(appointmentId);
    }

    /**
     * Gets an appointment by ID.
     */
    public AppointmentDTO getAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Cita", appointmentId));
        return enrichDTO(new AppointmentDTO(appointment));
    }

    /**
     * Counts pending appointments for today.
     */
    public long countPendingToday() {
        return appointmentRepository.findTodayAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING)
                .count();
    }

    private void validateAppointment(AppointmentDTO dto) {
        if (dto.getPatientId() == null) {
            throw new ValidationException("Patient ID is required");
        }
        if (dto.getDate() == null) {
            throw new ValidationException("Date is required");
        }
        if (dto.getTime() == null) {
            throw new ValidationException("Time is required");
        }
        if (!patientRepository.findById(dto.getPatientId()).isPresent()) {
            throw new NotFoundException("Paciente", dto.getPatientId());
        }
    }

    private AppointmentDTO enrichDTO(AppointmentDTO dto) {
        if (dto.getPatientId() != null) {
            patientRepository.findById(dto.getPatientId()).ifPresent(patient -> {
                dto.setPatientName(patient.getNombreCompleto());
                dto.setPatientAge(patient.getAge());
            });
        }

        if (dto.getDoctorId() != null) {
            userRepository.findById(dto.getDoctorId()).ifPresent(doctor -> {
                dto.setDoctorName(doctor.getFullName());
            });
        }

        return dto;
    }
}
