package com.vitalid.models;

import com.vitalid.models.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Doctor Entity
 * Represents a doctor in the system
 */
@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 100, nullable = false)
    private String specialty;

    @Column(columnDefinition = "TEXT")
    private String avatar;

    @Column(name = "medical_center_address", length = 500)
    private String medicalCenterAddress;

    @Column(length = 20, nullable = false)
    private String status = "OFFLINE";

    @Column(name = "unread_messages", nullable = false)
    private Integer unreadMessages = 0;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears = 0;

    @Column(name = "availability_start")
    private LocalTime availabilityStart;

    @Column(name = "availability_end")
    private LocalTime availabilityEnd;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = "OFFLINE";
        }
        if (this.unreadMessages == null) {
            this.unreadMessages = 0;
        }
        if (this.verified == null) {
            this.verified = false;
        }
        if (this.experienceYears == null) {
            this.experienceYears = 0;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}



