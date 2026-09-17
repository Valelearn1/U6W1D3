package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chats")
public class Chat {

    public static final String DEFAULT_TITLE = "Nuova chat";

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 120)
    private String title = DEFAULT_TITLE;

    @Column(nullable = false, length = 160)
    private String model;

    /** Proprietario della chat: ogni utente vede solo le proprie. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    protected Chat() {
        // richiesto da JPA
    }

    public Chat(User owner, String model) {
        this.owner = owner;
        this.model = model;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    /** Segna la chat come appena usata, cosi' risale in cima alla sidebar. */
    public void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
