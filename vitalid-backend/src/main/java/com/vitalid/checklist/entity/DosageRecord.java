package com.vitalid.checklist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DosageRecord Entity
 * Records when a medication dose was taken
 * 
 * TODO: Implement dosage record entity with:
 * - id (auto-generated)
 * - checklistId (foreign key)
 * - medicationId (foreign key)
 * - scheduledTime (the planned time)
 * - actualTime (when it was actually taken)
 * - isTaken (boolean)
 * - timestamp (when record was created)
 * - relationship to Checklist
 */
@Entity
@Table(name = "dosage_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DosageRecord {

    // TODO: Add entity properties and annotations

}
