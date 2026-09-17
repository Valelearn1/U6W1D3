package com.example.demo.dto;

import com.example.demo.entity.Chat;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatDetailDTO(
        UUID id,
        String title,
        String model,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt,
        List<MessageDTO> messages) {

    public static ChatDetailDTO from(Chat chat) {
        return new ChatDetailDTO(
                chat.getId(),
                chat.getTitle(),
                chat.getModel(),
                chat.isFavorite(),
                chat.getCreatedAt(),
                chat.getUpdatedAt(),
                chat.getMessages().stream().map(MessageDTO::from).toList());
    }
}
