package com.vitalid.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import com.vitalid.dtos.profile.ProfileResponse;
import com.vitalid.dtos.profile.ProfileUpdateRequest;
import com.vitalid.dtos.profile.PasswordChangeRequest;
import com.vitalid.models.User;
import com.vitalid.models.UserType;
import com.vitalid.repositories.UserRepository;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.repositories.DoctorRepository;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Profile Service
 * Handles user profile management
 */
@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toProfileResponse(user);
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);

        if (user.getType() == UserType.PATIENT) {
            // Upsert: create patient record if it doesn't exist yet (seeded users may lack one)
            var patient = patientRepository.findByUser_Id(userId).orElseGet(() -> {
                var newPatient = new com.vitalid.models.Patient();
                newPatient.setUser(user);
                newPatient.setIsActive(true);
                return newPatient;
            });
            if (request.getBloodType() != null) {
                patient.setBloodType(request.getBloodType());
            }
            if (request.getAllergies() != null) {
                patient.setAllergies(String.join(",", request.getAllergies()));
            }
            if (request.getDateOfBirth() != null) {
                patient.setDateOfBirth(request.getDateOfBirth());
            }
            patientRepository.save(patient);
        }

        if (user.getType() == UserType.DOCTOR) {
            var doctor = doctorRepository.findByUser_Id(userId);
            if (doctor != null) {
                if (request.getSpecialty() != null) {
                    doctor.setSpecialty(request.getSpecialty());
                }
                if (request.getAvatar() != null) {
                    doctor.setAvatar(request.getAvatar());
                }
                if (request.getExperienceYears() != null) {
                    doctor.setExperienceYears(request.getExperienceYears());
                }
                doctorRepository.save(doctor);
            }
        }

        return toProfileResponse(user);
    }

    /**
     * Upload avatar as Base64 data URI — works on any cloud platform (Railway, Render)
     * since no filesystem is needed.
     */
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }
        // 5 MB limit for avatars
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large (max 5 MB)");
        }

        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String dataUri = "data:" + contentType + ";base64," + base64;

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (user.getType() == UserType.DOCTOR) {
                var doctor = doctorRepository.findByUser_Id(userId);
                if (doctor != null) {
                    doctor.setAvatar(dataUri);
                    doctorRepository.save(doctor);
                }
            } else {
                var patient = patientRepository.findByUser_Id(userId).orElse(null);
                if (patient != null) {
                    patient.setAvatar(dataUri);
                    patientRepository.save(patient);
                }
            }
            return dataUri;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image");
        }
    }

    /**
     * Upload a file (image or PDF) for chat attachments.
     * Returns a Base64 data URI that can be sent as message content.
     */
    public String uploadChatFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown file type");
        }
        boolean isImage = contentType.startsWith("image/");
        boolean isPdf = contentType.equals("application/pdf");
        if (!isImage && !isPdf) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images and PDFs are allowed");
        }
        // 10 MB limit for chat files
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large (max 10 MB)");
        }

        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return "data:" + contentType + ";base64," + base64;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file");
        }
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        if (request.getOldPassword() == null || request.getNewPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old and new passwords are required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private ProfileResponse toProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setType(user.getType().name());

        if (user.getType() == UserType.PATIENT) {
            var patient = patientRepository.findByUser_Id(user.getId()).orElse(null);
            if (patient != null) {
                response.setBloodType(patient.getBloodType());
                response.setDateOfBirth(patient.getDateOfBirth());
                response.setAllergies(splitCsv(patient.getAllergies()));
                response.setAvatar(patient.getAvatar());
            }
        }

        if (user.getType() == UserType.DOCTOR) {
            var doctor = doctorRepository.findByUser_Id(user.getId());
            if (doctor != null) {
                response.setAvatar(doctor.getAvatar());
                response.setSpecialty(doctor.getSpecialty());
                response.setExperienceYears(doctor.getExperienceYears());
                response.setVerified(doctor.getVerified());
            }
        }

        return response;
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

}
