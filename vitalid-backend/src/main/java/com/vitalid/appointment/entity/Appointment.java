package com.vitalid.appointment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Appointment Entity
 * Represents a medical appointment between patient and doctor
 * 
 * TODO: Implement appointment entity with:
 * - id (auto-generated)
 * - patientId (foreign key)
 * - doctorId (foreign key)
 * - date
 * - time
 * - reason
 * - status (SCHEDULED, COMPLETED, CANCELLED, NO_SHOW)
 * - createdAt
 * - updatedAt
 * - relationship to patient
 * - relationship to doctor
 */
@Entity
@Table(name = "appointments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {

    // TODO: Add entity properties and annotations

}
