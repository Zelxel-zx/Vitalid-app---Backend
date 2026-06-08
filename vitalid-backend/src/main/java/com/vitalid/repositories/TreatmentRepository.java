package com.vitalid.repositories;

import com.vitalid.models.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Treatment Repository
 * Data access for Treatment entity
 */
@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    List<Treatment> findByPatientId(Long patientId);
    List<Treatment> findByDoctorId(Long doctorId);
    List<Treatment> findByStatus(String status);
}



