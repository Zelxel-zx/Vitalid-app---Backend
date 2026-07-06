package com.vitalid.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.vitalid.services.PatientService;
import com.vitalid.dtos.patient.PatientResponse;
import com.vitalid.dtos.patient.PatientRequest;
import com.vitalid.exception.ApiResponse;
import com.vitalid.models.User;
import com.vitalid.models.Appointment;
import com.vitalid.repositories.UserRepository;
import com.vitalid.repositories.AppointmentRepository;
import com.vitalid.repositories.TreatmentRepository;
import com.vitalid.exception.ResourceNotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Patient Controller
 * Handles HTTP requests for patient management
 */
@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

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
    @Operation(summary = "Crear perfil de paciente", description = "Crea un nuevo perfil de paciente con los datos de salud. Requiere autenticaciÃ³n JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paciente creado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invÃ¡lidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ApiResponse<PatientResponse> createPatient(@RequestBody PatientRequest request) {
        // Extract email from JWT token in SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        
        System.out.println("DEBUG: Email from token: " + email);
        
        // Get user from database using email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("DEBUG: User not found with email: " + email);
                    return new ResourceNotFoundException("Usuario no encontrado con email: " + email);
                });
        
        System.out.println("DEBUG: User found with id: " + user.getId());
        
        Long userId = user.getId();
        
        PatientResponse patient = patientService.createPatient(userId, request);
        user.setProfileCompleted(true);
        userRepository.save(user);
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
     * Get patients filtered by doctor relationship.
     * A relationship exists when there is a non-cancelled appointment or treatment.
     */
    @GetMapping("/by-doctor/{doctorId}")
    public ApiResponse<List<PatientResponse>> getPatientsByDoctor(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);

        Set<Long> patientIds = new LinkedHashSet<>();
        appointments.stream()
                .filter(appointment -> !"CANCELLED".equalsIgnoreCase(appointment.getStatus()))
                .map(a -> a.getPatient().getId())
                .forEach(patientIds::add);

        treatmentRepository.findByDoctorId(doctorId).stream()
                .filter(treatment -> !"CANCELLED".equalsIgnoreCase(treatment.getStatus()))
                .map(treatment -> treatment.getPatient().getId())
                .forEach(patientIds::add);

        List<PatientResponse> patients = patientIds.stream()
                .map(id -> patientService.getPatientById(id))
                .toList();
        return ApiResponse.ok("Pacientes del doctor recuperados", patients);
    }


}


