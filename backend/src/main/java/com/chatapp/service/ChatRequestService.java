package com.chatapp.service;

import com.chatapp.dto.ChatRequestDto;
import com.chatapp.exception.ApiException;
import com.chatapp.model.ChatRequest;
import com.chatapp.model.RequestStatus;
import com.chatapp.model.User;
import com.chatapp.repository.ChatRequestRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Private chat requests.
 *
 * Rules:
 *  - You cannot send a request to yourself.
 *  - Only one PENDING request may exist between two users (in either direction).
 *  - If the two users are already connected (ACCEPTED), a new request is refused.
 *  - Only the RECIPIENT can accept or reject; only the SENDER can cancel; only while PENDING.
 *  - After REJECTED / CANCELLED, a new request may be sent.
 *
 * Every change is pushed in real time to both users on /user/queue/requests.
 */
@Service
public class ChatRequestService {

    public static final String REQUESTS_QUEUE = "/queue/requests";

    private final ChatRequestRepository repository;
    private final UserService userService;
    private final SimpMessagingTemplate messaging;

    public ChatRequestService(ChatRequestRepository repository, UserService userService,
                              SimpMessagingTemplate messaging) {
        this.repository = repository;
        this.userService = userService;
        this.messaging = messaging;
    }

    @Transactional
    public ChatRequestDto create(String senderUsername, String recipientUsername) {
        User sender = userService.getByUsername(senderUsername);
        User recipient = userService.getByUsername(recipientUsername);
        if (sender.getId().equals(recipient.getId())) {
            throw ApiException.badRequest("You cannot send a chat request to yourself");
        }
        if (!repository.findBetween(sender, recipient, RequestStatus.ACCEPTED).isEmpty()) {
            throw ApiException.conflict("You are already connected with " + recipient.getUsername(), null);
        }
        List<ChatRequest> pending = repository.findBetween(sender, recipient, RequestStatus.PENDING);
        if (!pending.isEmpty()) {
            boolean incoming = pending.get(0).getRecipient().getId().equals(sender.getId());
            throw ApiException.conflict(incoming
                    ? recipient.getUsername() + " has already sent you a request. Accept it from the Requests page."
                    : "A request to " + recipient.getUsername() + " is already pending", null);
        }
        ChatRequest saved = repository.save(new ChatRequest(sender, recipient));
        return notifyBoth(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatRequestDto> incoming(String username) {
        return repository.findByRecipientOrderByCreatedAtDesc(userService.getByUsername(username))
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRequestDto> outgoing(String username) {
        return repository.findBySenderOrderByCreatedAtDesc(userService.getByUsername(username))
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public long pendingIncomingCount(String username) {
        return repository.countByRecipientAndStatus(userService.getByUsername(username), RequestStatus.PENDING);
    }

    @Transactional
    public ChatRequestDto accept(Long id, String username) {
        return respond(id, username, RequestStatus.ACCEPTED);
    }

    @Transactional
    public ChatRequestDto reject(Long id, String username) {
        return respond(id, username, RequestStatus.REJECTED);
    }

    @Transactional
    public ChatRequestDto cancel(Long id, String username) {
        ChatRequest request = findPending(id, username);
        if (!request.getSender().getUsername().equals(username)) {
            throw ApiException.forbidden("Only the sender can cancel this request");
        }
        request.setStatus(RequestStatus.CANCELLED);
        request.setRespondedAt(Instant.now());
        return notifyBoth(repository.save(request));
    }

    /** True when the two users have an accepted request between them (needed for private messaging). */
    @Transactional(readOnly = true)
    public boolean areConnected(User a, User b) {
        return !repository.findBetween(a, b, RequestStatus.ACCEPTED).isEmpty();
    }

    private ChatRequestDto respond(Long id, String username, RequestStatus newStatus) {
        ChatRequest request = findPending(id, username);
        if (!request.getRecipient().getUsername().equals(username)) {
            throw ApiException.forbidden("Only the recipient can respond to this request");
        }
        request.setStatus(newStatus);
        request.setRespondedAt(Instant.now());
        return notifyBoth(repository.save(request));
    }

    private ChatRequest findPending(Long id, String username) {
        ChatRequest request = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Chat request not found"));
        User me = userService.getByUsername(username);
        if (!request.involves(me)) {
            // Do not reveal that the request exists.
            throw ApiException.notFound("Chat request not found");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw ApiException.conflict("This request is already " + request.getStatus().name().toLowerCase(), null);
        }
        return request;
    }

    private ChatRequestDto notifyBoth(ChatRequest request) {
        ChatRequestDto dto = toDto(request);
        messaging.convertAndSendToUser(request.getSender().getUsername(), REQUESTS_QUEUE, dto);
        messaging.convertAndSendToUser(request.getRecipient().getUsername(), REQUESTS_QUEUE, dto);
        return dto;
    }

    private ChatRequestDto toDto(ChatRequest r) {
        return new ChatRequestDto(r.getId(), userService.toDto(r.getSender()), userService.toDto(r.getRecipient()),
                r.getStatus().name(), r.getCreatedAt(), r.getRespondedAt());
    }
}
