package com.vitalid.checklist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * Checklist Entity
 * Represents a medication adherence checklist for a patient
 * 
 * TODO: Implement checklist entity with:
 * - id (auto-generated)
 * - patientId (foreign key)
 * - medicationId (foreign key)
 * - createdDate
 * - updatedDate
 * - relationship to medications
 * - relationship to scheduled times
 * - relationship to dosage records
 */
@Entity
@Table(name = "checklists")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Checklist {

    // TODO: Add entity properties and annotations

}
