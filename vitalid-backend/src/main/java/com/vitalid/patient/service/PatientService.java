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

    /**
     * Get all active patients
     */
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get patient by ID
     */
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));
        return toResponse(patient);
    }

    /**
     * Get patient by user ID
     */
    public PatientResponse getPatientByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found for user id: " + userId));
        return toResponse(patient);
    }

    /**
     * Create a new patient
     */
    public PatientResponse createPatient(PatientRequest request) {
        if (request.getUserId() == null) {
            throw new InvalidPatientException("User ID is required");
        }

        if (request.getDateOfBirth() == null) {
            throw new InvalidPatientException("Date of birth is required");
        }

        Patient patient = new Patient();
        patient.setUser(new User());
        patient.getUser().setId(request.getUserId());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setBloodType(request.getBloodType());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setZipCode(request.getZipCode());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());
        patient.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Patient savedPatient = patientRepository.save(patient);
        return toResponse(savedPatient);
    }

    /**
     * Update an existing patient
     */
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));

        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getBloodType() != null) {
            patient.setBloodType(request.getBloodType());
        }
        if (request.getPhoneNumber() != null) {
            patient.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            patient.setCity(request.getCity());
        }
        if (request.getState() != null) {
            patient.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            patient.setZipCode(request.getZipCode());
        }
        if (request.getMedicalHistory() != null) {
            patient.setMedicalHistory(request.getMedicalHistory());
        }
        if (request.getAllergies() != null) {
            patient.setAllergies(request.getAllergies());
        }
        if (request.getIsActive() != null) {
            patient.setIsActive(request.getIsActive());
        }

        Patient updatedPatient = patientRepository.save(patient);
        return toResponse(updatedPatient);
    }

    /**
     * Delete a patient (soft delete)
     */
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));
        patient.setIsActive(false);
        patientRepository.save(patient);
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

    /**
     * Get patients by city
     */
    public List<PatientResponse> getPatientsByCity(String city) {
        return patientRepository.findByCity(city)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Patient entity to PatientResponse DTO
     */
    private PatientResponse toResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setUserId(patient.getUser().getId());
        response.setEmail(patient.getUser().getEmail());
        response.setName(patient.getUser().getName());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setBloodType(patient.getBloodType());
        response.setPhone(patient.getPhoneNumber());
        response.setAddress(patient.getAddress());
        response.setCity(patient.getCity());
        response.setState(patient.getState());
        response.setZipCode(patient.getZipCode());
        response.setMedicalHistory(patient.getMedicalHistory());
        response.setAllergies(patient.getAllergies());
        response.setIsActive(patient.getIsActive());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());
        return response;
    }
}

