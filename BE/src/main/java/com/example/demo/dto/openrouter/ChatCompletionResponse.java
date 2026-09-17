package com.example.demo.dto.openrouter;

import java.util.List;

/**
 * Risposta di OpenRouter. Teniamo solo i campi che ci servono: i campi
 * ignoti (usage, created, ...) vengono scartati da Jackson senza errori.
 */
public record ChatCompletionResponse(
        String id, String model, List<Choice> choices, ApiError error) {

    public record Choice(WireMessage message) {
    }

    public record ApiError(String message, Integer code) {
    }
}
