package com.vitalid.treatment.repository;

import com.vitalid.treatment.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Treatment Repository
 * Data access for Treatment entity
 * 
 * TODO: Implement query methods:
 * - findByPatientId(String patientId)
 * - findByDoctorId(Integer doctorId)
 * - findByStatus(String status)
 * - findByPatientIdAndStatus(String patientId, String status)
 * - findAllByStatusOrderByCreatedAtDesc(String status)
 */
@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, String> {

    // TODO: Add custom query methods

}
