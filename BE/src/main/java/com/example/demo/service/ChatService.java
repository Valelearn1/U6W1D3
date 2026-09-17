package com.example.demo.service;

import com.example.demo.config.OpenRouterProperties;
import com.example.demo.dto.*;
import com.example.demo.dto.openrouter.WireMessage;
import com.example.demo.entity.Chat;
import com.example.demo.entity.Message;
import com.example.demo.entity.MessageRole;
import com.example.demo.entity.User;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.MessageRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private static final int TITLE_MAX = 60;

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final OpenRouterService openRouter;
    private final OpenRouterProperties properties;

    public ChatService(
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            OpenRouterService openRouter,
            OpenRouterProperties properties) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.openRouter = openRouter;
        this.properties = properties;
    }

    // ---------- lettura ----------

    @Transactional(readOnly = true)
    public List<ChatSummaryDTO> listChats(User owner) {
        return chatRepository.findAllByOwnerOrderByUpdatedAtDesc(owner).stream()
                .map(ChatSummaryDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatDetailDTO getChat(UUID id, User owner) {
        return ChatDetailDTO.from(loadWithMessages(id, owner));
    }

    public List<ModelOptionDTO> listModels() {
        return properties.getModels().stream()
                .map(option -> new ModelOptionDTO(option.getId(), option.getLabel()))
                .toList();
    }

    // ---------- scrittura ----------

    @Transactional
    public ChatDetailDTO createChat(CreateChatRequest request, User owner) {
        String model = (request == null || request.model() == null || request.model().isBlank())
                ? openRouter.defaultModel()
                : request.model();
        return ChatDetailDTO.from(chatRepository.save(new Chat(owner, model)));
    }

    /**
     * Il giro completo: salva il prompt, interroga il modello, salva la risposta.
     *
     * <p>Volutamente NON annotato {@code @Transactional}: la chiamata HTTP puo'
     * durare anche un minuto e tenere aperta una transazione per tutto quel
     * tempo bloccherebbe una connessione del pool inutilmente. Ogni salvataggio
     * ha la sua piccola transazione.
     */
    public MessagePairDTO sendMessage(UUID chatId, SendMessageRequest request, User owner) {
        String content = request == null ? null : request.content();
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Il messaggio non puo' essere vuoto.");
        }
        String prompt = content.trim();

        Chat chat = loadWithMessages(chatId, owner);

        // Lo storico va rispedito a ogni richiesta: l'API non ricorda nulla.
        List<WireMessage> conversation = new ArrayList<>();
        for (Message stored : chat.getMessages()) {
            conversation.add(stored.getRole() == MessageRole.USER
                    ? WireMessage.user(stored.getContent())
                    : WireMessage.assistant(stored.getContent()));
        }
        conversation.add(WireMessage.user(prompt));

        Message userMessage =
                messageRepository.save(new Message(chat, MessageRole.USER, prompt));

        String answer = openRouter.complete(chat.getModel(), conversation);

        Message assistantMessage =
                messageRepository.save(new Message(chat, MessageRole.ASSISTANT, answer));

        // Alla prima domanda la chat prende il nome dal prompt.
        if (Chat.DEFAULT_TITLE.equals(chat.getTitle())) {
            chat.setTitle(deriveTitle(prompt));
        }
        chat.touch();
        chatRepository.save(chat);

        return new MessagePairDTO(
                MessageDTO.from(userMessage), MessageDTO.from(assistantMessage));
    }

    /** PUT: sostituzione completa: tutti i campi devono arrivare valorizzati. */
    @Transactional
    public ChatSummaryDTO replaceChat(UUID id, UpdateChatRequest request, User owner) {
        if (request.title() == null || request.title().isBlank()
                || request.model() == null || request.model().isBlank()
                || request.favorite() == null) {
            throw new IllegalArgumentException(
                    "PUT richiede title, model e favorite tutti valorizzati. "
                            + "Per modificare un solo campo usa PATCH.");
        }

        Chat chat = load(id, owner);
        chat.setTitle(trimTitle(request.title()));
        chat.setModel(request.model());
        chat.setFavorite(request.favorite());
        return ChatSummaryDTO.from(chatRepository.save(chat));
    }

    /** PATCH: modifica parziale, applica solo i campi presenti nel body. */
    @Transactional
    public ChatSummaryDTO patchChat(UUID id, UpdateChatRequest request, User owner) {
        if (request.title() == null && request.model() == null && request.favorite() == null) {
            throw new IllegalArgumentException("Nessun campo da aggiornare.");
        }

        Chat chat = load(id, owner);
        if (request.title() != null && !request.title().isBlank()) {
            chat.setTitle(trimTitle(request.title()));
        }
        if (request.model() != null && !request.model().isBlank()) {
            chat.setModel(request.model());
        }
        if (request.favorite() != null) {
            chat.setFavorite(request.favorite());
        }
        return ChatSummaryDTO.from(chatRepository.save(chat));
    }

    @Transactional
    public void deleteChat(UUID id, User owner) {
        // I messaggi se ne vanno con la chat grazie al cascade + orphanRemoval.
        chatRepository.delete(load(id, owner));
    }

    // ---------- helper ----------

    // L'owner e' parte della query: la chat di un altro utente semplicemente
    // "non esiste", cosi' non si rivela nemmeno che quell'id sia valido.
    private Chat load(UUID id, User owner) {
        return chatRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Chat " + id + " non trovata."));
    }

    private Chat loadWithMessages(UUID id, User owner) {
        return chatRepository.findWithMessagesByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Chat " + id + " non trovata."));
    }

    private String trimTitle(String title) {
        String clean = title.trim();
        return clean.length() <= TITLE_MAX ? clean : clean.substring(0, TITLE_MAX);
    }

    private String deriveTitle(String prompt) {
        String oneLine = prompt.replaceAll("\\s+", " ").trim();
        if (oneLine.length() <= TITLE_MAX) {
            return oneLine;
        }
        // Taglia all'ultimo spazio per non spezzare una parola a meta'.
        String cut = oneLine.substring(0, TITLE_MAX);
        int lastSpace = cut.lastIndexOf(' ');
        return (lastSpace > 20 ? cut.substring(0, lastSpace) : cut) + "...";
    }
}
