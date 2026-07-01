package com.vitalid.models;

import com.vitalid.models.Medication;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * DosageRecord Entity
 * Records when a medication dose was taken
 */
@Entity
@Table(name = "dosage_records")
@Data
@NoArgsConstructor
public class DosageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @ManyToOne
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @ManyToOne
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(length = 10)
    private String scheduledTime;

    @Column(name = "actual_time", length = 10)
    private String actualTime;

    @Column(name = "is_taken")
    private Boolean isTaken = false;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}



