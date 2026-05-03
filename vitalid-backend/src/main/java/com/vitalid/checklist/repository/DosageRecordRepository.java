package com.vitalid.checklist.repository;

import com.vitalid.checklist.entity.DosageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Dosage Record Repository
 * Data access for DosageRecord entity
 * 
 * TODO: Implement query methods:
 * - findByChecklistId(String checklistId)
 * - findByMedicationId(String medicationId)
 * - findByChecklistIdAndIsTakenFalse(String checklistId)
 * - findByMedicationIdAndTimestampBetween(String medId, LocalDateTime from, LocalDateTime to)
 */
@Repository
public interface DosageRecordRepository extends JpaRepository<DosageRecord, String> {

    // TODO: Add custom query methods

}
