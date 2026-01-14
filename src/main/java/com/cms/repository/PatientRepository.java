package com.cms.repository;

import com.cms.core.Repository;
import com.cms.domain.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends Repository<Patient, Integer> {

    Optional<Patient> findByCedula(String cedula);

    List<Patient> findByNombreContaining(String nombre);

    List<Patient> findByApellidoContaining(String apellido);

    List<Patient> search(String query);

    boolean existsByCedula(String cedula);

    long count();
}
