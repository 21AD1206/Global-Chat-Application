package com.chatapp.controller;

import com.chatapp.dto.ConversationDto;
import com.chatapp.dto.MessageDto;
import com.chatapp.dto.PrivateSendRequest;
import com.chatapp.exception.ApiException;
import com.chatapp.service.PrivateChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Private messaging.
 *  - REST  GET  /api/private/conversations            : people you can chat with
 *  - REST  GET  /api/private/{username}/messages      : history (participants only)
 *  - STOMP SEND /app/private.send  { to, content }    : delivered on /user/queue/private
 */
@RestController
public class PrivateChatController {

    private final PrivateChatService service;

    public PrivateChatController(PrivateChatService service) {
        this.service = service;
    }

    @GetMapping("/api/private/conversations")
    public List<ConversationDto> conversations(Principal principal) {
        return service.conversations(principal.getName());
    }

    @GetMapping("/api/private/{username}/messages")
    public List<MessageDto> history(@PathVariable String username,
                                    @RequestParam(defaultValue = "100") int limit,
                                    Principal principal) {
        return service.history(principal.getName(), username, limit);
    }

    @MessageMapping("/private.send")
    public void send(@Payload PrivateSendRequest request, Principal principal) {
        if (request == null) {
            throw ApiException.badRequest("Message is required");
        }
        service.send(principal.getName(), request.to(), request.content());
    }
}
