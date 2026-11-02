package com.chatapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Creates and verifies JSON Web Tokens (JWT).
 *
 * A token contains: subject = username, a unique id (jti), issue time and expiry time,
 * and is signed with HMAC-SHA256 using the secret in application.properties.
 * Because it is signed, the browser cannot alter it without the signature check failing.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;
    private final TokenBlacklist blacklist;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
                      TokenBlacklist blacklist) {
        if (secret.length() < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
        this.blacklist = blacklist;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** Returns the claims if the token is well-formed, correctly signed, not expired and not logged out. */
    public Optional<Claims> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            if (blacklist.isRevoked(claims.getId())) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Logout: remember the token id until it would have expired anyway. */
    public void revoke(String token) {
        validate(token).ifPresent(c -> blacklist.revoke(c.getId(), c.getExpiration().toInstant()));
    }

    /** Extracts the raw token from an "Authorization: Bearer xxx" header value. */
    public static String stripBearer(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }
}
