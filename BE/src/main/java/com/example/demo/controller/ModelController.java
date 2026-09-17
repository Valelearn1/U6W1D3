package com.example.demo.controller;

import com.example.demo.dto.ModelOptionDTO;
import com.example.demo.service.ChatService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Alimenta la tendina "scegli il modello" del frontend. */
@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ChatService chatService;

    public ModelController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ModelOptionDTO> list() {
        return chatService.listModels();
    }
}
