package com.chatapp.controller;

import com.chatapp.dto.MessageDto;
import com.chatapp.dto.SendMessageRequest;
import com.chatapp.service.GlobalChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Global (public) chat.
 *  - REST  GET  /api/global/messages   : load history when the chat page opens
 *  - STOMP SEND /app/global.send       : post a new message (broadcast on /topic/global)
 */
@RestController
public class GlobalChatController {

    private final GlobalChatService service;

    public GlobalChatController(GlobalChatService service) {
        this.service = service;
    }

    @GetMapping("/api/global/messages")
    public List<MessageDto> history(@RequestParam(defaultValue = "100") int limit) {
        return service.history(limit);
    }

    @MessageMapping("/global.send")
    public void send(@Payload SendMessageRequest request, Principal principal) {
        service.send(principal.getName(), request == null ? null : request.content());
    }
}
