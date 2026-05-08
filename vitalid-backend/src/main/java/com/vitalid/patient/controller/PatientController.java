package com.vitalid.patient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.vitalid.patient.service.PatientService;
import com.vitalid.patient.dto.PatientResponse;
import com.vitalid.patient.dto.PatientRequest;
import com.vitalid.exception.ApiResponse;
import com.vitalid.auth.entity.User;
import java.util.List;

/**
 * Patient Controller
 * Handles HTTP requests for patient management
 */
@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * Get all patients
     */
    @GetMapping
    public ApiResponse<List<PatientResponse>> getAllPatients() {
        List<PatientResponse> patients = patientService.getAllPatients();
        return ApiResponse.ok("Pacientes recuperados exitosamente", patients);
    }

    /**
     * Get patient by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<PatientResponse> getPatientById(@PathVariable Long id) {
        PatientResponse patient = patientService.getPatientById(id);
        return ApiResponse.ok("Paciente recuperado exitosamente", patient);
    }

    /**
     * Create a new patient
     */
    @PostMapping
    public ApiResponse<PatientResponse> createPatient(@RequestBody PatientRequest request) {
        // Extract userId from JWT token in SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        
        PatientResponse patient = patientService.createPatient(userId, request);
        return ApiResponse.ok("Paciente creado exitosamente", patient);
    }

    /**
     * Update an existing patient
     */
    @PutMapping("/{id}")
    public ApiResponse<PatientResponse> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientRequest request) {
        PatientResponse patient = patientService.updatePatient(id, request);
        return ApiResponse.ok("Paciente actualizado exitosamente", patient);
    }

    /**
     * Delete a patient (soft delete)
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ApiResponse.ok("Paciente eliminado exitosamente", null);
    }

    /**
     * Get patients by blood type
     */
    @GetMapping("/filter/blood-type/{bloodType}")
    public ApiResponse<List<PatientResponse>> getPatientsByBloodType(@PathVariable String bloodType) {
        List<PatientResponse> patients = patientService.getPatientsByBloodType(bloodType);
        return ApiResponse.ok("Paciente recuperado exitosamente", patients);
    }

    /**
     * Get patients by allergy
     */
    @GetMapping("/filter/allergy/{allergy}")
    public ApiResponse<List<PatientResponse>> getPatientsByAllergy(@PathVariable String allergy) {
        List<PatientResponse> patients = patientService.getPatientsByAllergy(allergy);
        return ApiResponse.ok("Paciente recuperado exitosamente", patients);
    }

    /**
     * Get patients by city
     */
    @GetMapping("/filter/city/{city}")
    public ApiResponse<List<PatientResponse>> getPatientsByCity(@PathVariable String city) {
        List<PatientResponse> patients = patientService.getPatientsByCity(city);
        return ApiResponse.ok("Pacientes por ciudad recuperados exitosamente", patients);
    }

    /**
     * Get patients by state
     */
    @GetMapping("/filter/state/{state}")
    public ApiResponse<List<PatientResponse>> getPatientsByState(@PathVariable String state) {
        List<PatientResponse> patients = patientService.getPatientsByState(state);
        return ApiResponse.ok("Pacientes por estado recuperados exitosamente", patients);
    }

    /**
     * Get patients by zip code
     */
    @GetMapping("/filter/zipcode/{zipCode}")
    public ApiResponse<List<PatientResponse>> getPatientsByZipCode(@PathVariable String zipCode) {
        List<PatientResponse> patients = patientService.getPatientsByZipCode(zipCode);
        return ApiResponse.ok("Pacientes por código postal recuperados exitosamente", patients);
    }

}

