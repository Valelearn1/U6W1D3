package com.example.demo.dto;

/** Risposta di register e login: il token da conservare e chi sei. */
public record AuthResponse(String token, long expiresInMillis, UserDTO user) {
}
