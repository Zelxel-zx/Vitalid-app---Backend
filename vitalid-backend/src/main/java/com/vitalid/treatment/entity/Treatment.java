package com.vitalid.treatment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * Treatment Entity
 * Represents a medical treatment for a patient
 * 
 * TODO: Implement treatment entity with:
 * - id (auto-generated)
 * - patientId (foreign key)
 * - doctorId (foreign key)
 * - title
 * - status (ACTIVE, COMPLETED, PENDING)
 * - progress (0-100)
 * - nextAppointment (date)
 * - createdAt
 * - updatedAt
 * - relationship to medications
 * - relationship to doctor
 * - relationship to patient
 */
@Entity
@Table(name = "treatments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Treatment {

    // TODO: Add entity properties and annotations

}
