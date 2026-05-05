package com.vitalid.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest (

    @Email(message = "Email inválido")
    @NotBlank(message = "Email es obligatorio")
    String email,

    @NotBlank(message = "Contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "Contraseña debe tener entre 6 y 100 caracteres")
    String password,

    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 100, message = "Nombre debe tener como máximo 100 caracteres")
    String name,

    @NotBlank(message = "Teléfono es obligatorio")
    @Size(max = 20, message = "Teléfono debe tener como máximo 20 caracteres")
    String phone,

    @NotBlank(message = "Tipo es obligatorio")
    UserType type

) {}
