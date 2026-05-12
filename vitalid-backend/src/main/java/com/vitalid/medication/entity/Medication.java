package com.vitalid.medication.entity;

import com.vitalid.doctor.entity.Doctor;
import com.vitalid.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Medication Entity
 * Represents a medication prescribed to a patient
 */
@Entity
@Table(name = "medications")
@Data
@NoArgsConstructor
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 100)
    private String dosage;

    @Column(length = 50)
    private String frequency;

    @Column(length = 150)
    private String prescribedBy;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_pills")
    private Integer totalPills;

    @Column(name = "pills_remaining")
    private Integer pillsRemaining;

    @Column(name = "side_effects", length = 1000)
    private String sideEffects;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

