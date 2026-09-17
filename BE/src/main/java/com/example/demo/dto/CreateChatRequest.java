package com.example.demo.dto;

/** Body di POST /api/chats. Se model è null si usa quello di default. */
public record CreateChatRequest(String model) {
}
