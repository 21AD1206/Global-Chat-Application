package com.chatapp.repository;

import com.chatapp.model.GlobalMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GlobalMessageRepository extends JpaRepository<GlobalMessage, Long> {

    /** Newest first; the service reverses the list so the UI gets oldest first. */
    @Query("SELECT m FROM GlobalMessage m ORDER BY m.sentAt DESC, m.id DESC")
    List<GlobalMessage> findLatest(Pageable pageable);
}
