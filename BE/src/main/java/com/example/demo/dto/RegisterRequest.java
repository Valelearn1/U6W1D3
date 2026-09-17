package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "L'email e' obbligatoria.")
        @Email(message = "Email non valida.")
        String email,

        @NotBlank(message = "La password e' obbligatoria.")
        @Size(min = 8, message = "La password deve avere almeno 8 caratteri.")
        String password,

        @NotBlank(message = "Il nome e' obbligatorio.")
        @Size(max = 80, message = "Il nome e' troppo lungo.")
        String displayName) {
}
