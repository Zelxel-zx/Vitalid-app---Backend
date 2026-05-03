package com.vitalid.health.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * HealthMetric Entity
 * Represents a health measurement for a patient
 * 
 * TODO: Implement health metric entity with:
 * - id (auto-generated)
 * - patientId (foreign key)
 * - metric (BLOOD_PRESSURE, BLOOD_SUGAR, WEIGHT, HEART_RATE, etc.)
 * - value (numeric value)
 * - unit (mg/dl, mmHg, kg, bpm, etc.)
 * - timestamp (when measurement was taken)
 * - notes (optional notes)
 * - createdAt
 * - relationship to patient
 */
@Entity
@Table(name = "health_metrics")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthMetric {

    // TODO: Add entity properties and annotations

}
