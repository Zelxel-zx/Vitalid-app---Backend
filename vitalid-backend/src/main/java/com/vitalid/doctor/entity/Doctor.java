package com.vitalid.doctor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Doctor Entity
 * Represents a doctor in the system
 * 
 * TODO: Implement doctor entity with:
 * - id (auto-generated)
 * - userId (foreign key to User)
 * - specialty
 * - avatar
 * - status (ONLINE, OFFLINE, BUSY)
 * - unreadMessages (count)
 * - createdAt
 * - updatedAt
 * - relationship to User
 * - relationship to Appointments
 */
@Entity
@Table(name = "doctors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {

    // TODO: Add entity properties and annotations

}
