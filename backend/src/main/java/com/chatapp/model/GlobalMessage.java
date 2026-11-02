package com.chatapp.model;

import jakarta.persistence.*;

import java.time.Instant;

/** A message posted in the shared public (global) chat room. */
@Entity
@Table(name = "global_messages", indexes = @Index(name = "idx_global_sent_at", columnList = "sent_at"))
public class GlobalMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    public GlobalMessage() {
    }

    public GlobalMessage(User sender, String content) {
        this.sender = sender;
        this.content = content;
        this.sentAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getSender() { return sender; }
    public String getContent() { return content; }
    public Instant getSentAt() { return sentAt; }
}
