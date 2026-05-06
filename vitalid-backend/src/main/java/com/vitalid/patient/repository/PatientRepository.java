package com.vitalid.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.vitalid.patient.entity.Patient;
import java.util.Optional;
import java.util.List;

/**
 * Patient Repository
 * Handles database operations for Patient entity
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    List<Patient> findByBloodType(String bloodType);

    List<Patient> findByAllergiesContaining(String allergy);
}
