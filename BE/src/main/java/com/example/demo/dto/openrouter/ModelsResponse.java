package com.example.demo.dto.openrouter;

import java.util.List;

/**
 * Risposta di GET https://openrouter.ai/api/v1/models.
 *
 * <p>I nomi dei campi sono in snake_case perche' devono combaciare con quelli
 * del JSON di OpenRouter. Tutto cio' che non ci serve (description, links,
 * supported_parameters...) viene scartato da Jackson senza errori.
 */
public record ModelsResponse(List<Model> data) {

    public record Model(
            String id,
            String name,
            Architecture architecture,
            Pricing pricing,
            Long context_length) {
    }

    public record Architecture(List<String> output_modalities) {
    }

    public record Pricing(String prompt, String completion) {
    }
}
