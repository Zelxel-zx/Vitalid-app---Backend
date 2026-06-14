package com.vitalid.repositories;

import com.vitalid.models.DosageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Dosage Record Repository
 * Data access for DosageRecord entity
 */
@Repository
public interface DosageRecordRepository extends JpaRepository<DosageRecord, Long> {

    List<DosageRecord> findByChecklistId(Long checklistId);
    List<DosageRecord> findByMedicationId(Long medicationId);
    List<DosageRecord> findByChecklistIdAndIsTakenFalse(Long checklistId);
    List<DosageRecord> findByTreatmentId(Long treatmentId);
    List<DosageRecord> findByTreatmentIdAndScheduledDate(Long treatmentId, LocalDate scheduledDate);
    Optional<DosageRecord> findFirstByMedicationIdAndScheduledDateAndScheduledTime(
            Long medicationId, LocalDate scheduledDate, String scheduledTime);
}



