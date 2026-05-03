package com.vitalid.checklist.repository;

import com.vitalid.checklist.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Checklist Repository
 * Data access for Checklist entity
 * 
 * TODO: Implement query methods:
 * - findByPatientId(String patientId)
 * - findByMedicationId(String medicationId)
 * - findByPatientIdAndCreatedDate(String patientId, LocalDate date)
 * - findTodayChecklistByPatientId(String patientId)
 */
@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, String> {

    // TODO: Add custom query methods

}
