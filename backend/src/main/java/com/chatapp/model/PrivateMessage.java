package com.chatapp.model;

import jakarta.persistence.*;

import java.time.Instant;

/** A one-to-one message between two users who have an accepted chat request. */
@Entity
@Table(name = "private_messages", indexes = {
        @Index(name = "idx_pm_sender_recipient", columnList = "sender_id, recipient_id"),
        @Index(name = "idx_pm_sent_at", columnList = "sent_at")
})
public class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    public PrivateMessage() {
    }

    public PrivateMessage(User sender, User recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.sentAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getSender() { return sender; }
    public User getRecipient() { return recipient; }
    public String getContent() { return content; }
    public Instant getSentAt() { return sentAt; }
}
