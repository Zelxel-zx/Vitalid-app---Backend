package com.vitalid.models;

import com.vitalid.models.Patient;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * HealthMetric Entity
 * Represents a health measurement for a patient
 */
@Entity
@Table(name = "health_metrics")
@Data
@NoArgsConstructor
public class HealthMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(length = 100, nullable = false)
    private String metric;

    @Column(nullable = false)
    private Double value;

    @Column(length = 20)
    private String unit;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.timestamp = this.timestamp == null ? LocalDateTime.now() : this.timestamp;
    }
}



