package com.chatapp.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWTs are stateless, so "logout" is implemented by remembering the ids of logged-out tokens
 * until they expire. Kept in memory: a server restart forgets the list (acceptable for a prototype;
 * a production system would use Redis or a database table).
 */
@Component
public class TokenBlacklist {

    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String tokenId, Instant expiresAt) {
        if (tokenId != null) {
            revoked.put(tokenId, expiresAt);
        }
    }

    public boolean isRevoked(String tokenId) {
        return tokenId != null && revoked.containsKey(tokenId);
    }

    /** Every 10 minutes, drop entries whose token has expired anyway. */
    @Scheduled(fixedRate = 600_000)
    public void cleanup() {
        Instant now = Instant.now();
        revoked.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }
}
