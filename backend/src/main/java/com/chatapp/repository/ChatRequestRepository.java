package com.chatapp.repository;

import com.chatapp.model.ChatRequest;
import com.chatapp.model.RequestStatus;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRequestRepository extends JpaRepository<ChatRequest, Long> {

    List<ChatRequest> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<ChatRequest> findBySenderOrderByCreatedAtDesc(User sender);

    /** All requests between two users with the given status, in either direction, newest first. */
    @Query("SELECT r FROM ChatRequest r WHERE r.status = :status AND " +
            "((r.sender = :a AND r.recipient = :b) OR (r.sender = :b AND r.recipient = :a)) " +
            "ORDER BY r.createdAt DESC")
    List<ChatRequest> findBetween(@Param("a") User a, @Param("b") User b, @Param("status") RequestStatus status);

    /** Accepted requests the user is part of: these are the user's private conversations. */
    @Query("SELECT r FROM ChatRequest r WHERE r.status = com.chatapp.model.RequestStatus.ACCEPTED AND " +
            "(r.sender = :u OR r.recipient = :u)")
    List<ChatRequest> findAcceptedFor(@Param("u") User user);

    long countByRecipientAndStatus(User recipient, RequestStatus status);
}
