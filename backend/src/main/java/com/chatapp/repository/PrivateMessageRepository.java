package com.chatapp.repository;

import com.chatapp.model.PrivateMessage;
import com.chatapp.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    /** Messages between two users (both directions), newest first. */
    @Query("SELECT m FROM PrivateMessage m WHERE " +
            "(m.sender = :a AND m.recipient = :b) OR (m.sender = :b AND m.recipient = :a) " +
            "ORDER BY m.sentAt DESC, m.id DESC")
    List<PrivateMessage> findConversation(@Param("a") User a, @Param("b") User b, Pageable pageable);
}
