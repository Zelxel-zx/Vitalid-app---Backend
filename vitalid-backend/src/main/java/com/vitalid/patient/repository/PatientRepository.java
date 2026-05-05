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

    /**
     * Find a patient by user ID
     */
    Optional<Patient> findByUserId(Long userId);

    /**
     * Find all active patients
     */
    List<Patient> findByIsActiveTrue();

    /**
     * Find patients by blood type
     */
    List<Patient> findByBloodType(String bloodType);

    /**
     * Find patients by city
     */
    List<Patient> findByCity(String city);

    /**
     * Check if a patient exists for a user
     */
    boolean existsByUserId(Long userId);
}

