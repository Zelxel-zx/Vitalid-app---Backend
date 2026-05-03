package com.vitalid.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * User Entity
 * Represents a user in the system (Patient or Doctor)
 * 
 * TODO: Implement user entity with:
 * - id (auto-generated)
 * - email (unique)
 * - password (hashed)
 * - name
 * - phone
 * - type (PATIENT, DOCTOR)
 * - createdAt
 * - updatedAt
 * - relationships to other entities
 */
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    
    // TODO: Add entity properties and annotations

}
