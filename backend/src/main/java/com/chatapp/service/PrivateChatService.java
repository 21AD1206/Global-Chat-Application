package com.chatapp.service;

import com.chatapp.dto.ConversationDto;
import com.chatapp.dto.MessageDto;
import com.chatapp.exception.ApiException;
import com.chatapp.model.ChatRequest;
import com.chatapp.model.PrivateMessage;
import com.chatapp.model.User;
import com.chatapp.repository.ChatRequestRepository;
import com.chatapp.repository.PrivateMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * One-to-one messaging. Every read and every write first checks that the current user is one of the
 * two participants AND that an ACCEPTED chat request exists between them. Otherwise: 403 Forbidden.
 */
@Service
public class PrivateChatService {

    public static final String PRIVATE_QUEUE = "/queue/private";

    private final PrivateMessageRepository messageRepository;
    private final ChatRequestRepository requestRepository;
    private final ChatRequestService requestService;
    private final UserService userService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messaging;

    public PrivateChatService(PrivateMessageRepository messageRepository, ChatRequestRepository requestRepository,
                              ChatRequestService requestService, UserService userService,
                              PresenceService presenceService, SimpMessagingTemplate messaging) {
        this.messageRepository = messageRepository;
        this.requestRepository = requestRepository;
        this.requestService = requestService;
        this.userService = userService;
        this.presenceService = presenceService;
        this.messaging = messaging;
    }

    /** Everyone the user may chat privately with, most recent activity first. */
    @Transactional(readOnly = true)
    public List<ConversationDto> conversations(String username) {
        User me = userService.getByUsername(username);
        List<ConversationDto> result = new ArrayList<>();
        for (ChatRequest r : requestRepository.findAcceptedFor(me)) {
            User partner = r.getSender().getId().equals(me.getId()) ? r.getRecipient() : r.getSender();
            MessageDto last = messageRepository.findConversation(me, partner, PageRequest.of(0, 1))
                    .stream().findFirst().map(PrivateChatService::toDto).orElse(null);
            result.add(new ConversationDto(userService.toDto(partner), last, r.getRespondedAt()));
        }
        result.sort(Comparator.comparing((ConversationDto c) -> activity(c)).reversed());
        return result;
    }

    @Transactional(readOnly = true)
    public List<MessageDto> history(String username, String otherUsername, int limit) {
        User me = userService.getByUsername(username);
        User other = userService.getByUsername(otherUsername);
        requireConnected(me, other);
        int size = Math.max(1, Math.min(limit, 500));
        List<MessageDto> list = new ArrayList<>(messageRepository.findConversation(me, other, PageRequest.of(0, size))
                .stream().map(PrivateChatService::toDto).toList());
        Collections.reverse(list);
        return list;
    }

    /** 1) check permission  2) validate  3) save  4) deliver to both users' private queues. */
    @Transactional
    public MessageDto send(String username, String toUsername, String content) {
        User me = userService.getByUsername(username);
        User other = userService.getByUsername(toUsername);
        requireConnected(me, other);
        String text = MessageValidator.clean(content);
        PrivateMessage saved = messageRepository.save(new PrivateMessage(me, other, text));
        MessageDto dto = toDto(saved);
        // The recipient gets it, and so does the sender (keeps several open tabs of the sender in sync).
        messaging.convertAndSendToUser(other.getUsername(), PRIVATE_QUEUE, dto);
        messaging.convertAndSendToUser(me.getUsername(), PRIVATE_QUEUE, dto);
        presenceService.markLive(username);
        return dto;
    }

    private void requireConnected(User me, User other) {
        if (me.getId().equals(other.getId())) {
            throw ApiException.badRequest("You cannot chat privately with yourself");
        }
        if (!requestService.areConnected(me, other)) {
            throw ApiException.forbidden("Private chat with " + other.getUsername()
                    + " is not allowed until a chat request is accepted");
        }
    }

    private static Instant activity(ConversationDto c) {
        if (c.lastMessage() != null) {
            return c.lastMessage().sentAt();
        }
        return c.connectedSince() != null ? c.connectedSince() : Instant.EPOCH;
    }

    static MessageDto toDto(PrivateMessage m) {
        return new MessageDto(m.getId(), m.getSender().getUsername(), m.getSender().getName(),
                m.getRecipient().getUsername(), m.getContent(), m.getSentAt());
    }
}
