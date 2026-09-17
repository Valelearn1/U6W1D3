package com.example.demo.repository;

import com.example.demo.entity.Chat;
import com.example.demo.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    /** Sidebar: solo le chat di chi ha fatto la richiesta, le piu' recenti in cima. */
    List<Chat> findAllByOwnerOrderByUpdatedAtDesc(User owner);

    /**
     * Dettaglio. L'owner fa parte della query, non e' un controllo fatto dopo:
     * chiedere la chat di un altro utente da' "non trovata", non "vietato",
     * cosi' non si scopre nemmeno se quell'id esiste.
     */
    @EntityGraph(attributePaths = "messages")
    Optional<Chat> findWithMessagesByIdAndOwner(UUID id, User owner);

    Optional<Chat> findByIdAndOwner(UUID id, User owner);
}
