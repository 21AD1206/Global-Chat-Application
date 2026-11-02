package com.chatapp.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Listens to WebSocket life-cycle events published by Spring and forwards them to PresenceService.
 * A disconnect event fires when the user logs out, closes the tab, loses network
 * or stops answering heartbeats.
 */
@Component
public class PresenceEventListener {

    private final PresenceService presenceService;

    public PresenceEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        Principal user = event.getUser();
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
        if (user != null && sessionId != null) {
            presenceService.userConnected(user.getName(), sessionId);
        }
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            presenceService.userDisconnected(user.getName(), event.getSessionId());
        }
    }
}
