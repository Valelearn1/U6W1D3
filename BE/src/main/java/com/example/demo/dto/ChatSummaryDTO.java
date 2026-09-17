package com.example.demo.dto;

import com.example.demo.entity.Chat;
import java.time.Instant;
import java.util.UUID;

/** Riga della sidebar: niente messaggi, solo quello che serve all'elenco. */
public record ChatSummaryDTO(
        UUID id, String title, String model, boolean favorite, Instant updatedAt) {

    public static ChatSummaryDTO from(Chat chat) {
        return new ChatSummaryDTO(
                chat.getId(),
                chat.getTitle(),
                chat.getModel(),
                chat.isFavorite(),
                chat.getUpdatedAt());
    }
}
