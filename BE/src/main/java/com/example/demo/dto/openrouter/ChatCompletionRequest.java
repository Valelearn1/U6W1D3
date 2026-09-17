package com.example.demo.dto.openrouter;

import java.util.List;

/** Il corpo JSON del comando cURL: -d '{"model": ..., "messages": [...]}' */
public record ChatCompletionRequest(String model, List<WireMessage> messages) {
}
