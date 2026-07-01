package com.vitalid.services;

import com.vitalid.dtos.doctor.DoctorRequest;
import com.vitalid.dtos.doctor.DoctorResponse;
import com.vitalid.models.Doctor;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.repositories.DoctorRepository;
import com.vitalid.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ResourceAccessService resourceAccessService;
    private final UserRepository userRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         ResourceAccessService resourceAccessService,
                         UserRepository userRepository) {
        this.doctorRepository = doctorRepository;
        this.resourceAccessService = resourceAccessService;
        this.userRepository = userRepository;
    }

    @Transactional
    public DoctorResponse createOrCompleteProfile(DoctorRequest request) {
        User user = resourceAccessService.currentUser();
        if (user.getType() != UserType.DOCTOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only doctor users can create a doctor profile");
        }

        validateRequest(request);
        Doctor doctor = doctorRepository.findByUser_Id(user.getId());
        if (doctor == null) {
            doctor = new Doctor();
            doctor.setUser(user);
            doctor.setStatus("OFFLINE");
            doctor.setUnreadMessages(0);
            doctor.setVerified(false);
        }

        doctor.setSpecialty(request.specialty().trim());
        doctor.setAvatar(normalize(request.avatar()));
        doctor.setMedicalCenterAddress(normalize(request.medicalCenterAddress()));
        doctor.setExperienceYears(request.experienceYears() == null ? 0 : request.experienceYears());
        doctor.setAvailabilityStart(request.availabilityStart());
        doctor.setAvailabilityEnd(request.availabilityEnd());

        Doctor savedDoctor = doctorRepository.save(doctor);
        user.setProfileCompleted(true);
        userRepository.save(user);
        return toResponse(savedDoctor);
    }

    private void validateRequest(DoctorRequest request) {
        if (request == null || request.specialty() == null
                || request.specialty().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Doctor specialty is required");
        }
        if (request.specialty().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Doctor specialty cannot exceed 100 characters");
        }
        if (request.avatar() != null && request.avatar().trim().length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Doctor avatar cannot exceed 255 characters");
        }
        if (request.medicalCenterAddress() != null
                && request.medicalCenterAddress().trim().length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Medical center address cannot exceed 500 characters");
        }
        if (request.experienceYears() != null && request.experienceYears() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Experience years cannot be negative");
        }
        validateAvailability(request.availabilityStart(), request.availabilityEnd());
    }

    private void validateAvailability(LocalTime start, LocalTime end) {
        if ((start == null) != (end == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Availability start and end must be provided together");
        }
        if (start != null && !end.isAfter(start)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Availability end must be after start");
        }
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private DoctorResponse toResponse(Doctor doctor) {
        User user = doctor.getUser();
        return new DoctorResponse(
                doctor.getId(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                doctor.getSpecialty(),
                doctor.getAvatar(),
                doctor.getMedicalCenterAddress(),
                doctor.getStatus(),
                doctor.getUnreadMessages(),
                doctor.getVerified(),
                doctor.getExperienceYears(),
                doctor.getAvailabilityStart(),
                doctor.getAvailabilityEnd(),
                doctor.getCreatedAt(),
                doctor.getUpdatedAt());
    }
}
