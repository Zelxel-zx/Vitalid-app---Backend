package com.vitalid.appointment.repository;

import com.vitalid.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

/**
 * Appointment Repository
 * Data access for Appointment entity
 * 
 * TODO: Implement query methods:
 * - findByPatientId(String patientId, Pageable pageable)
 * - findByDoctorId(Integer doctorId)
 * - findByPatientIdAndStatus(String patientId, String status)
 * - findByDoctorIdAndStatus(Integer doctorId, String status)
 * - findByDateBetween(LocalDate from, LocalDate to)
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    // TODO: Add custom query methods

}
