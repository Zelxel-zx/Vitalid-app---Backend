package com.vitalid.dtos.doctor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record DoctorRequest(
        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 100, message = "La especialidad debe tener como máximo 100 caracteres")
        String specialty,

        @Size(max = 255, message = "El avatar debe tener como máximo 255 caracteres")
        String avatar,

        @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
        Integer experienceYears,

        LocalTime availabilityStart,
        LocalTime availabilityEnd
) {
}
