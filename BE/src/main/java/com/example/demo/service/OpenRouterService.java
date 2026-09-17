package com.example.demo.service;

import com.example.demo.config.OpenRouterProperties;
import com.example.demo.dto.openrouter.ChatCompletionRequest;
import com.example.demo.dto.openrouter.ChatCompletionResponse;
import com.example.demo.dto.openrouter.WireMessage;
import com.example.demo.exception.OpenRouterException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Traduzione in Java del comando cURL di OpenRouter.
 *
 * <pre>
 * curl https://openrouter.ai/api/v1/chat/completions   -> baseUrl + uri()
 *   -H "Authorization: Bearer $KEY"                    -> header(...)
 *   -H "Content-Type: application/json"                -> defaultHeader nel RestClient
 *   -d '{"model": ..., "messages": [...]}'             -> body(ChatCompletionRequest)
 * </pre>
 */
@Service
public class OpenRouterService {

    private final RestClient client;
    private final OpenRouterProperties properties;

    public OpenRouterService(RestClient openRouterClient, OpenRouterProperties properties) {
        this.client = openRouterClient;
        this.properties = properties;
    }

    /**
     * Manda l'intera conversazione al modello e restituisce il testo della risposta.
     * Lo storico va spedito tutto a ogni giro: l'API e' senza memoria.
     */
    public String complete(String model, List<WireMessage> conversation) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new OpenRouterException(
                    "Manca la API key: esporta la variabile d'ambiente OPENROUTER_API_KEY "
                            + "prima di avviare il backend.");
        }

        ChatCompletionResponse response;
        try {
            response = client.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(new ChatCompletionRequest(model, conversation))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, res) -> {
                        String body = new String(
                                res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        throw new OpenRouterException(
                                "OpenRouter ha risposto " + res.getStatusCode() + " - " + body);
                    })
                    .body(ChatCompletionResponse.class);
        } catch (ResourceAccessException e) {
            throw new OpenRouterException(
                    "Impossibile contattare OpenRouter: " + e.getMessage(), e);
        }

        return extractContent(response);
    }

    private String extractContent(ChatCompletionResponse response) {
        if (response == null) {
            throw new OpenRouterException("OpenRouter ha restituito una risposta vuota.");
        }
        if (response.error() != null) {
            throw new OpenRouterException("OpenRouter: " + response.error().message());
        }
        if (response.choices() == null || response.choices().isEmpty()) {
            throw new OpenRouterException("OpenRouter non ha restituito nessuna risposta.");
        }

        WireMessage message = response.choices().get(0).message();
        if (message == null || message.content() == null || message.content().isBlank()) {
            // Puo' capitare coi modelli reasoning che esauriscono i token
            // ragionando senza arrivare a scrivere la risposta finale.
            throw new OpenRouterException(
                    "Il modello ha risposto senza contenuto testuale. Riprova o cambia modello.");
        }

        return message.content().trim();
    }

    public String defaultModel() {
        return properties.getDefaultModel();
    }
}
