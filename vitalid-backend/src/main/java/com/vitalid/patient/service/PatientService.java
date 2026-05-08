package com.vitalid.patient.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.vitalid.patient.entity.Patient;
import com.vitalid.patient.repository.PatientRepository;
import com.vitalid.patient.dto.PatientResponse;
import com.vitalid.patient.dto.PatientRequest;
import com.vitalid.patient.exception.PatientNotFoundException;
import com.vitalid.patient.exception.InvalidPatientException;
import com.vitalid.auth.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Patient Service
 * Handles patient management operations
 */
@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get patient by ID
     */
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado con id: " + id));
        return toResponse(patient);
    }

    /**
     * Create a new patient
     * Note: Patient inherits from User, so User record must be created first via AuthService.register()
     */
    public PatientResponse createPatient(Long userId, PatientRequest request) {
        if (request.getDateOfBirth() == null) {
            throw new InvalidPatientException("Se requiere la fecha de nacimiento");
        }

        Patient patient = new Patient();
        patient.setId(userId);
        patient.setType(UserType.PATIENT);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setBloodType(request.getBloodType());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());

        Patient savedPatient = patientRepository.save(patient);
        return toResponse(savedPatient);
    }

    /**
     * Update an existing patient
     */
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado con id: " + id));

        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getBloodType() != null) {
            patient.setBloodType(request.getBloodType());
        }
        if (request.getMedicalHistory() != null) {
            patient.setMedicalHistory(request.getMedicalHistory());
        }
        if (request.getAllergies() != null) {
            patient.setAllergies(request.getAllergies());
        }

        Patient updatedPatient = patientRepository.save(patient);
        return toResponse(updatedPatient);
    }

    /**
     * Delete a patient (hard delete)
     */
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException("Paciente no encontrado con id: " + id);
        }
        patientRepository.deleteById(id);
    }

    /**
     * Get patients by blood type
     */
    public List<PatientResponse> getPatientsByBloodType(String bloodType) {
        return patientRepository.findByBloodType(bloodType)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PatientResponse> getPatientsByAllergy(String allergy) {
        return patientRepository.findByAllergiesContaining(allergy)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Patient entity to PatientResponse DTO
     */
    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
            patient.getDateOfBirth(),
            patient.getBloodType(),
            patient.getMedicalHistory(),
            patient.getAllergies()
        );
    }
}
