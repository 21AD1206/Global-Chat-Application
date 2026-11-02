package com.chatapp.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A request from one user (sender) to another (recipient) to start a private chat.
 * Private messaging between two users is allowed only when an ACCEPTED request exists between them.
 */
@Entity
@Table(name = "chat_requests")
public class ChatRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public ChatRequest() {
    }

    public ChatRequest(User sender, User recipient) {
        this.sender = sender;
        this.recipient = recipient;
        this.status = RequestStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public boolean involves(User user) {
        return sender.getId().equals(user.getId()) || recipient.getId().equals(user.getId());
    }

    public Long getId() { return id; }
    public User getSender() { return sender; }
    public User getRecipient() { return recipient; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Instant respondedAt) { this.respondedAt = respondedAt; }
}
