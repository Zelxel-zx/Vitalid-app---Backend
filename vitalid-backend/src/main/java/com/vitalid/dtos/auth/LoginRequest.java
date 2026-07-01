package com.vitalid.dtos.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(


    @Email(message = "Email invÃ¡lido")
    @NotBlank(message = "Email es obligatorio")
    String email,


    @NotBlank(message = "ContraseÃ±a es obligatoria")
    @Size(min = 6, max = 100, message = "ContraseÃ±a debe tener entre 6 y 100 caracteres")
    String password

) {}




