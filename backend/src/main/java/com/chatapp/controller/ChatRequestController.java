package com.chatapp.controller;

import com.chatapp.dto.ChatRequestDto;
import com.chatapp.dto.CreateChatRequest;
import com.chatapp.service.ChatRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/** Private chat requests: send, list, accept, reject, cancel. */
@RestController
@RequestMapping("/api/requests")
public class ChatRequestController {

    private final ChatRequestService service;

    public ChatRequestController(ChatRequestService service) {
        this.service = service;
    }

    /** POST /api/requests  { toUsername } -> 201 */
    @PostMapping
    public ResponseEntity<ChatRequestDto> create(@Valid @RequestBody CreateChatRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(principal.getName(), request.toUsername()));
    }

    @GetMapping("/incoming")
    public List<ChatRequestDto> incoming(Principal principal) {
        return service.incoming(principal.getName());
    }

    @GetMapping("/outgoing")
    public List<ChatRequestDto> outgoing(Principal principal) {
        return service.outgoing(principal.getName());
    }

    /** GET /api/requests/pending-count -> { "count": 2 }  (badge in the navigation bar) */
    @GetMapping("/pending-count")
    public Map<String, Long> pendingCount(Principal principal) {
        return Map.of("count", service.pendingIncomingCount(principal.getName()));
    }

    @PostMapping("/{id}/accept")
    public ChatRequestDto accept(@PathVariable Long id, Principal principal) {
        return service.accept(id, principal.getName());
    }

    @PostMapping("/{id}/reject")
    public ChatRequestDto reject(@PathVariable Long id, Principal principal) {
        return service.reject(id, principal.getName());
    }

    @PostMapping("/{id}/cancel")
    public ChatRequestDto cancel(@PathVariable Long id, Principal principal) {
        return service.cancel(id, principal.getName());
    }
}
