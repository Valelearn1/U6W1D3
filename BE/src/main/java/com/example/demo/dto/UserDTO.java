package com.example.demo.dto;

import com.example.demo.entity.User;
import java.util.UUID;

/** L'utente come lo vede il frontend: nessun hash, nessuna password. */
public record UserDTO(UUID id, String email, String displayName) {

    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
