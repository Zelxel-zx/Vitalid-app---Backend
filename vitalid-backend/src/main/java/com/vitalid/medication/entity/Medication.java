package com.vitalid.medication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Medication Entity
 * Represents a medication prescribed to a patient
 * 
 * TODO: Implement medication entity with:
 * - id (auto-generated)
 * - patientId (foreign key)
 * - doctorId (foreign key)
 * - name
 * - dosage
 * - frequency (DAILY, TWICE_DAILY, THREE_TIMES_DAILY, WEEKLY)
 * - prescribedBy (doctor name)
 * - startDate
 * - endDate
 * - createdAt
 * - relationship to patient
 * - relationship to doctor
 */
@Entity
@Table(name = "medications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Medication {

    // TODO: Add entity properties and annotations

}
