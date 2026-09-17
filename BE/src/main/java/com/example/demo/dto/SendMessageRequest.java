package com.example.demo.dto;

/** Body di POST /api/chats/{id}/messages. */
public record SendMessageRequest(String content) {
}
