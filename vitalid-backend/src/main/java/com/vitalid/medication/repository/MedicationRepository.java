package com.vitalid.medication.repository;

import com.vitalid.medication.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Medication Repository
 * Data access for Medication entity
 * 
 * TODO: Implement query methods:
 * - findByPatientId(String patientId)
 * - findByDoctorId(Integer doctorId)
 * - findActiveByPatientId(String patientId)
 * - findByPatientIdAndEndDateIsNull(String patientId)
 */
@Repository
public interface MedicationRepository extends JpaRepository<Medication, String> {

    // TODO: Add custom query methods

}
