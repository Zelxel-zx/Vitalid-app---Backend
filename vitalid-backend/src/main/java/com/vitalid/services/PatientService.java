package com.vitalid.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.vitalid.models.Patient;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.dtos.patient.PatientResponse;
import com.vitalid.dtos.patient.PatientRequest;
import com.vitalid.repositories.UserRepository;
import com.vitalid.exception.PatientNotFoundException;
import com.vitalid.exception.InvalidPatientException;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
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
    private UserRepository userRepository;

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
        if (request.dateOfBirth() == null) {
            throw new InvalidPatientException("Se requiere la fecha de nacimiento");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setBloodType(request.bloodType());
        patient.setAvatar(normalize(request.avatar()));
        patient.setAddress(request.address());
        patient.setCity(request.city());
        patient.setState(request.state());
        patient.setZipCode(request.zipCode());
        patient.setMedicalHistory(request.medicalHistory());
        patient.setAllergies(request.allergies());

        Patient savedPatient = patientRepository.save(patient);
        return toResponse(savedPatient);
    }

    /**
     * Update an existing patient
     */
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado con id: " + id));

            if(request.address() != null) {
                patient.setAddress(request.address());
            }

            if(request.city() != null) {
                patient.setCity(request.city());
            }
            if(request.state() != null) {
                patient.setState(request.state());
            }

            if(request.bloodType() != null) {
                patient.setBloodType(request.bloodType());
            }
            if (request.avatar() != null) {
                patient.setAvatar(normalize(request.avatar()));
            }
            
            if (request.dateOfBirth() != null) {
                patient.setDateOfBirth(request.dateOfBirth());
            }
            if (request.zipCode() != null) {
                patient.setZipCode(request.zipCode());
            }
            if (request.medicalHistory() != null) {
                patient.setMedicalHistory(request.medicalHistory());
            }
            if (request.allergies() != null) {
                patient.setAllergies(request.allergies());
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
        return new PatientResponse(
            patient.getId(),
            patient.getDateOfBirth(),
            patient.getBloodType(),
            patient.getAvatar(),
            patient.getAddress(),
            patient.getCity(),
            patient.getState(),
            patient.getZipCode(),
            patient.getMedicalHistory(),
            patient.getAllergies(),
            patient.getIsActive(),
            patient.getCreatedAt(),
            patient.getUpdatedAt(),
            patient.getUser() != null ? patient.getUser().getId() : null,
            patient.getUser() != null ? patient.getUser().getName() : "Usuario Desconocido",
            patient.getUser() != null ? patient.getUser().getEmail() : ""
        );
    }

    /**
     * Get patients by state
     */
    public List<PatientResponse> getPatientsByState(String state) {
        return patientRepository.findByState(state)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

}



