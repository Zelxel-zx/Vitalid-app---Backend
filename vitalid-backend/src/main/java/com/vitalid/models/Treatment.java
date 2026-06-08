package com.vitalid.models;

import com.vitalid.models.Doctor;
import com.vitalid.models.Patient;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Treatment Entity
 * Represents a medical treatment for a patient
 */
@Entity
@Table(name = "treatments")
@Data
@NoArgsConstructor
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 30)
    private String status = "ACTIVE";

    @Column
    private Integer progress = 0;

    @Column(name = "next_appointment")
    private LocalDate nextAppointment;

    @Column(columnDefinition = "TEXT")
    private String medications;

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



