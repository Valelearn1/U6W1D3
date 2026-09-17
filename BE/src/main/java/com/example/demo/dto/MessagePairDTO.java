package com.example.demo.dto;

/** Risposta a un invio: il messaggio salvato dell'utente e quello del modello. */
public record MessagePairDTO(MessageDTO userMessage, MessageDTO assistantMessage) {
}
