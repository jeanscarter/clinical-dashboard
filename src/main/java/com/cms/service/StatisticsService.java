package com.cms.service;

import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsService {

    private final PatientRepository patientRepository;
    private final ClinicalHistoryRepository historyRepository;

    public StatisticsService(PatientRepository patientRepository,
            ClinicalHistoryRepository historyRepository) {
        this.patientRepository = patientRepository;
        this.historyRepository = historyRepository;
    }

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.totalPatients = patientRepository.count();
        stats.totalConsultations = historyRepository.count();
        stats.consultationsToday = getConsultationsForDate(LocalDate.now());
        stats.consultationsThisMonth = getConsultationsForMonth(LocalDate.now());
        stats.avgConsultationsPerPatient = stats.totalPatients > 0
                ? (double) stats.totalConsultations / stats.totalPatients
                : 0;
        return stats;
    }

    public long getConsultationsForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return historyRepository.findAll().stream()
                .filter(h -> h.getFechaConsulta() != null &&
                        !h.getFechaConsulta().isBefore(start) &&
                        h.getFechaConsulta().isBefore(end))
                .count();
    }

    public long getConsultationsForMonth(LocalDate date) {
        LocalDateTime start = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = date.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        return historyRepository.findAll().stream()
                .filter(h -> h.getFechaConsulta() != null &&
                        !h.getFechaConsulta().isBefore(start) &&
                        h.getFechaConsulta().isBefore(end))
                .count();
    }

    public Map<String, Long> getConsultationsByDoctor() {
        return historyRepository.findAll().stream()
                .filter(h -> h.getMedico() != null && !h.getMedico().isEmpty())
                .collect(Collectors.groupingBy(
                        ClinicalHistory::getMedico,
                        Collectors.counting()));
    }

    public Map<String, Long> getConsultationsByMonth(int year) {
        Map<String, Long> monthlyStats = new HashMap<>();
        String[] months = { "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };

        for (int month = 1; month <= 12; month++) {
            final int m = month;
            long count = historyRepository.findAll().stream()
                    .filter(h -> h.getFechaConsulta() != null &&
                            h.getFechaConsulta().getYear() == year &&
                            h.getFechaConsulta().getMonthValue() == m)
                    .count();
            monthlyStats.put(months[month - 1], count);
        }
        return monthlyStats;
    }

    public Map<String, Long> getPatientsByGender() {
        return patientRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSexo() != null ? p.getSexo() : "No especificado",
                        Collectors.counting()));
    }

    public List<PatientConsultationCount> getTopPatientsByConsultations(int limit) {
        Map<Integer, Long> consultationCounts = historyRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        ClinicalHistory::getPatientId,
                        Collectors.counting()));

        return consultationCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(entry -> {
                    Patient patient = patientRepository.findById(entry.getKey()).orElse(null);
                    String name = patient != null ? patient.getNombreCompleto() : "Desconocido";
                    return new PatientConsultationCount(entry.getKey(), name, entry.getValue());
                })
                .collect(Collectors.toList());
    }

    public static class DashboardStats {
        public long totalPatients;
        public long totalConsultations;
        public long consultationsToday;
        public long consultationsThisMonth;
        public double avgConsultationsPerPatient;
    }

    public record PatientConsultationCount(Integer patientId, String patientName, Long count) {
    }
}
