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
    List<Treatment> findByPatientIdAndDoctorId(Long patientId, Long doctorId);
    List<Treatment> findByPatient_User_Id(Long userId);
    List<Treatment> findByDoctor_User_Id(Long userId);
    List<Treatment> findByStatus(String status);
    List<Treatment> findByPatientIdAndStatus(Long patientId, String status);
    List<Treatment> findByDoctorIdAndStatus(Long doctorId, String status);
    List<Treatment> findByPatientIdAndDoctorIdAndStatus(Long patientId, Long doctorId, String status);
}



