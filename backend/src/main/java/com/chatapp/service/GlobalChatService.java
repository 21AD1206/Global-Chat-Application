package com.chatapp.service;

import com.chatapp.dto.MessageDto;
import com.chatapp.model.GlobalMessage;
import com.chatapp.model.User;
import com.chatapp.repository.GlobalMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The shared public chat room. */
@Service
public class GlobalChatService {

    public static final String GLOBAL_TOPIC = "/topic/global";

    private final GlobalMessageRepository repository;
    private final UserService userService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messaging;

    public GlobalChatService(GlobalMessageRepository repository, UserService userService,
                             PresenceService presenceService, SimpMessagingTemplate messaging) {
        this.repository = repository;
        this.userService = userService;
        this.presenceService = presenceService;
        this.messaging = messaging;
    }

    /** The latest {@code limit} messages, oldest first (the order a chat window shows them). */
    @Transactional(readOnly = true)
    public List<MessageDto> history(int limit) {
        int size = Math.max(1, Math.min(limit, 500));
        List<MessageDto> latest = new ArrayList<>(repository.findLatest(PageRequest.of(0, size))
                .stream().map(GlobalChatService::toDto).toList());
        Collections.reverse(latest);
        return latest;
    }

    /** 1) validate  2) save to DB  3) broadcast to everyone subscribed to /topic/global. */
    @Transactional
    public MessageDto send(String username, String content) {
        String text = MessageValidator.clean(content);
        User sender = userService.getByUsername(username);
        GlobalMessage saved = repository.save(new GlobalMessage(sender, text));
        MessageDto dto = toDto(saved);
        messaging.convertAndSend(GLOBAL_TOPIC, dto);
        presenceService.markLive(username);
        return dto;
    }

    static MessageDto toDto(GlobalMessage m) {
        return new MessageDto(m.getId(), m.getSender().getUsername(), m.getSender().getName(),
                null, m.getContent(), m.getSentAt());
    }
}
