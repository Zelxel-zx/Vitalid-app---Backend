package com.vitalid.checklist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ScheduledTime Entity
 * Represents a scheduled time for taking medication
 * 
 * TODO: Implement scheduled time entity with:
 * - id (auto-generated)
 * - checklistId (foreign key)
 * - time (e.g., "07:00", "14:00")
 * - dayOfWeek (optional, for weekly schedules)
 * - relationship to Checklist
 */
@Entity
@Table(name = "scheduled_times")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledTime {

    // TODO: Add entity properties and annotations

}
