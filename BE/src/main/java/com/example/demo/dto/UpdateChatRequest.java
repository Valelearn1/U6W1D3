package com.example.demo.dto;

/**
 * Usato sia da PUT che da PATCH.
 * PUT pretende tutti i campi valorizzati (sostituzione completa),
 * PATCH applica solo quelli non null (modifica parziale).
 */
public record UpdateChatRequest(String title, String model, Boolean favorite) {
}
