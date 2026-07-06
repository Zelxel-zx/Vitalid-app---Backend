package com.vitalid.repositories;

import com.vitalid.models.PatientNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientNoteRepository extends JpaRepository<PatientNote, Long> {

    List<PatientNote> findByDoctorIdAndPatientIdOrderByCreatedAtDesc(Long doctorId, Long patientId);
}
