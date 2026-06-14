package com.vitalid.repositories;

import com.vitalid.models.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Checklist Repository
 * Data access for Checklist entity
 */
@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    List<Checklist> findByPatientId(Long patientId);
    List<Checklist> findByMedicationId(Long medicationId);
    Optional<Checklist> findFirstByTreatmentId(Long treatmentId);
}



