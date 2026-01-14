package com.cms.repository;

import com.cms.core.Repository;
import com.cms.domain.ClinicalHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface ClinicalHistoryRepository extends Repository<ClinicalHistory, Integer> {

    List<ClinicalHistory> findByPatientId(Integer patientId);

    List<ClinicalHistory> findByFechaConsultaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<ClinicalHistory> findByMedico(String medico);

    List<ClinicalHistory> findByDiagnosticoContaining(String diagnostico);

    List<ClinicalHistory> findRecentByPatientId(Integer patientId, int limit);

    long countByPatientId(Integer patientId);

    long count();
}
