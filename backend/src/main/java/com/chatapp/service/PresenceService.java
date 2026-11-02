package com.chatapp.service;

import com.chatapp.dto.PresenceDto;
import com.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of who is ONLINE and who is LIVE.
 *
 * ONLINE = the user has at least one open WebSocket connection (any page of the app, any tab).
 *          Set when the STOMP connection is established, cleared when the last connection closes.
 *
 * LIVE   = the user is actively in a chat screen right now. While a chat screen (global or private)
 *          is open AND the browser tab is visible, the page sends a heartbeat to /app/presence.live
 *          every 15 seconds; sending a message counts as a heartbeat too. If no heartbeat arrives
 *          within app.presence.live-timeout-seconds (45 s), the LIVE badge is removed.
 *          Leaving the chat screen sends /app/presence.idle so the badge is removed immediately.
 *
 * Every change is broadcast to /topic/presence so all browsers update their indicators in real time.
 * State is kept in memory (see README "Limitations").
 */
@Service
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);
    public static final String PRESENCE_TOPIC = "/topic/presence";

    /** username -> ids of that user's open WebSocket sessions */
    private final Map<String, Set<String>> sessions = new ConcurrentHashMap<>();
    /** username -> time of the last LIVE heartbeat */
    private final Map<String, Instant> liveSince = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messaging;
    private final UserRepository userRepository;
    private final Duration liveTimeout;

    public PresenceService(SimpMessagingTemplate messaging, UserRepository userRepository,
                           @Value("${app.presence.live-timeout-seconds}") long liveTimeoutSeconds) {
        this.messaging = messaging;
        this.userRepository = userRepository;
        this.liveTimeout = Duration.ofSeconds(liveTimeoutSeconds);
    }

    public void userConnected(String username, String sessionId) {
        Set<String> userSessions = sessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet());
        boolean wasOffline = userSessions.isEmpty();
        userSessions.add(sessionId);
        if (wasOffline) {
            log.info("{} is ONLINE", username);
            broadcast(username);
        }
    }

    public void userDisconnected(String username, String sessionId) {
        Set<String> userSessions = sessions.get(username);
        if (userSessions == null) {
            return;
        }
        userSessions.remove(sessionId);
        if (userSessions.isEmpty()) {
            sessions.remove(username);
            liveSince.remove(username);
            updateLastSeen(username);
            log.info("{} is OFFLINE", username);
            broadcast(username);
        }
    }

    /** Heartbeat from an open, visible chat screen (or a message being sent). */
    public void markLive(String username) {
        if (!isOnline(username)) {
            return;
        }
        Instant previous = liveSince.put(username, Instant.now());
        if (previous == null) {
            broadcast(username);
        }
    }

    /** The user left the chat screen or hid the tab. */
    public void markIdle(String username) {
        if (liveSince.remove(username) != null) {
            broadcast(username);
        }
    }

    public boolean isOnline(String username) {
        Set<String> s = sessions.get(username);
        return s != null && !s.isEmpty();
    }

    public boolean isLive(String username) {
        return isOnline(username) && liveSince.containsKey(username);
    }

    /** State of every user that is currently online. Offline users are simply absent. */
    public List<PresenceDto> snapshot() {
        return sessions.keySet().stream()
                .map(u -> new PresenceDto(u, true, isLive(u)))
                .toList();
    }

    /** Every 5 seconds remove LIVE from users whose heartbeat stopped (tab closed, browser frozen...). */
    @Scheduled(fixedRate = 5_000)
    public void expireLive() {
        Instant cutoff = Instant.now().minus(liveTimeout);
        liveSince.forEach((username, last) -> {
            if (last.isBefore(cutoff) && liveSince.remove(username, last)) {
                log.info("{} LIVE expired", username);
                broadcast(username);
            }
        });
    }

    private void broadcast(String username) {
        messaging.convertAndSend(PRESENCE_TOPIC, new PresenceDto(username, isOnline(username), isLive(username)));
    }

    private void updateLastSeen(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setLastSeen(Instant.now());
            userRepository.save(u);
        });
    }
}
