package com.example.demo.dto;

import com.example.demo.entity.Message;
import java.time.Instant;
import java.util.UUID;

public record MessageDTO(UUID id, String role, String content, Instant createdAt) {

    public static MessageDTO from(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt());
    }
}
