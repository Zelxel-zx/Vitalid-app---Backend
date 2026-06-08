package com.vitalid.repositories;

import com.vitalid.models.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Medication Repository
 * Data access for Medication entity
 */
@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByPatientId(Long patientId);
    List<Medication> findByPatient_User_Id(Long userId);
}



