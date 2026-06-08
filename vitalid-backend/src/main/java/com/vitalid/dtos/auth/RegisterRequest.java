package com.vitalid.dtos.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.vitalid.models.UserType;


public record RegisterRequest(

    @Email(message = "Email invÃ¡lido")
    @NotBlank(message = "Email es obligatorio")
    String email,

    @NotBlank(message = "ContraseÃ±a es obligatoria")
    @Size(min = 6, max = 100, message = "ContraseÃ±a debe tener entre 6 y 100 caracteres")
    String password,

    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 100, message = "Nombre debe tener como mÃ¡ximo 100 caracteres")
    String name,

    @NotBlank(message = "TelÃ©fono es obligatorio")
    @Size(max = 20, message = "TelÃ©fono debe tener como mÃ¡ximo 20 caracteres")
    String phone,

    @NotNull(message = "Tipo es obligatorio")
    UserType type
) {}





