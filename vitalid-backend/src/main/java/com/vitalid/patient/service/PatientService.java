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
import com.vitalid.auth.entity.UserType;
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
        if (request.dateOfBirth() == null) {
            throw new InvalidPatientException("Se requiere la fecha de nacimiento");
        }

        Patient patient = new Patient();
        patient.setId(userId);
        patient.setType(UserType.PATIENT);
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setBloodType(request.bloodType());
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
            patient.getAddress(),
            patient.getCity(),
            patient.getState(),
            patient.getZipCode(),
            patient.getMedicalHistory(),
            patient.getAllergies(),
            patient.getIsActive(),
            patient.getCreatedAt(),
            patient.getUpdatedAt()
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
     * Get patients by zip code
     */
    public List<PatientResponse> getPatientsByZipCode(String zipCode) {
        return patientRepository.findByZipCode(zipCode)
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

}

