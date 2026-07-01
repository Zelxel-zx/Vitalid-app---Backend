package com.vitalid.repositories;

import com.vitalid.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Appointment Repository
 * Data access for Appointment entity
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);
    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);
    List<Appointment> findByDateBetween(LocalDate from, LocalDate to);
    List<Appointment> findAllByOrderByDateAscTimeAsc();
    List<Appointment> findByPatientIdOrderByDateAscTimeAsc(Long patientId);
    List<Appointment> findByDoctorIdOrderByDateAscTimeAsc(Long doctorId);
    boolean existsByDoctorIdAndDateAndTimeAndStatusNot(
            Long doctorId, LocalDate date, LocalTime time, String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Appointment appointment
            SET appointment.status = 'COMPLETED'
            WHERE UPPER(appointment.status) = 'SCHEDULED'
              AND (
                appointment.date < :cutoffDate
                OR (appointment.date = :cutoffDate AND appointment.time <= :cutoffTime)
              )
            """)
    int completeExpiredAppointments(
            @Param("cutoffDate") LocalDate cutoffDate,
            @Param("cutoffTime") LocalTime cutoffTime);
}



