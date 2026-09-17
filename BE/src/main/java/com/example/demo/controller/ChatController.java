package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.User;
import com.example.demo.service.ChatService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Tutti gli endpoint richiedono il token (vedi SecurityConfig).
 * {@code @AuthenticationPrincipal} inietta l'utente che il
 * JwtAuthenticationFilter ha ricavato dal token: ogni operazione e' quindi
 * automaticamente limitata alle chat di chi l'ha richiesta.
 */
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** GET /api/chats - elenco per la sidebar (preferite + precedenti). */
    @GetMapping
    public List<ChatSummaryDTO> list(@AuthenticationPrincipal User user) {
        return chatService.listChats(user);
    }

    /** GET /api/chats/{id} - una chat con tutta la sua cronologia. */
    @GetMapping("/{id}")
    public ChatDetailDTO detail(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return chatService.getChat(id, user);
    }

    /** POST /api/chats - crea una conversazione vuota. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDetailDTO create(
            @RequestBody(required = false) CreateChatRequest request,
            @AuthenticationPrincipal User user) {
        return chatService.createChat(request, user);
    }

    /** POST /api/chats/{id}/messages - manda un prompt e ottiene la risposta. */
    @PostMapping("/{id}/messages")
    public MessagePairDTO send(
            @PathVariable UUID id,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User user) {
        return chatService.sendMessage(id, request, user);
    }

    /** PUT /api/chats/{id} - sostituisce titolo, modello e preferita insieme. */
    @PutMapping("/{id}")
    public ChatSummaryDTO replace(
            @PathVariable UUID id,
            @RequestBody UpdateChatRequest request,
            @AuthenticationPrincipal User user) {
        return chatService.replaceChat(id, request, user);
    }

    /** PATCH /api/chats/{id} - cambia un campo solo (stella, rinomina, modello). */
    @PatchMapping("/{id}")
    public ChatSummaryDTO patch(
            @PathVariable UUID id,
            @RequestBody UpdateChatRequest request,
            @AuthenticationPrincipal User user) {
        return chatService.patchChat(id, request, user);
    }

    /** DELETE /api/chats/{id} - elimina la chat e i suoi messaggi. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id, @AuthenticationPrincipal User user) {
        chatService.deleteChat(id, user);
        return ResponseEntity.noContent().build();
    }
}
