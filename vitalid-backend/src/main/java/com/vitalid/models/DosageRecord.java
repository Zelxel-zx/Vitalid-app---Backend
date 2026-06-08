package com.vitalid.models;

import com.vitalid.models.Medication;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
        this.timestamp = LocalDateTime.now();
    }
}



