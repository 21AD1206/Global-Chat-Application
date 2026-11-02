package com.chatapp.controller;

import com.chatapp.dto.PresenceDto;
import com.chatapp.service.PresenceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Online / LIVE status.
 *  - REST  GET  /api/presence          : who is online/LIVE right now (initial state)
 *  - STOMP SEND /app/presence.live     : heartbeat from an open chat screen
 *  - STOMP SEND /app/presence.idle     : user left the chat screen
 *  Changes are broadcast on /topic/presence.
 */
@RestController
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/api/presence")
    public List<PresenceDto> snapshot() {
        return presenceService.snapshot();
    }

    @MessageMapping("/presence.live")
    public void live(Principal principal) {
        presenceService.markLive(principal.getName());
    }

    @MessageMapping("/presence.idle")
    public void idle(Principal principal) {
        presenceService.markIdle(principal.getName());
    }
}
