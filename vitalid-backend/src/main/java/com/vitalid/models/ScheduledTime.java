package com.vitalid.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ScheduledTime Entity
 * Represents a scheduled time for taking medication
 */
@Entity
@Table(name = "scheduled_times")
@Data
@NoArgsConstructor
public class ScheduledTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Column(length = 10, nullable = false)
    private String time;
}



