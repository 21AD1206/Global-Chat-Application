package com.chatapp.config;

import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtAuthFilter;
import com.chatapp.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Security for WebSocket frames.
 *
 * CONNECT:   the browser must send "Authorization: Bearer <jwt>" as a STOMP header. If the token is
 *            valid, the username is attached to the WebSocket session; otherwise the connection is refused.
 * SUBSCRIBE: allowed only to /topic/** (public) and /user/queue/** (your own private queue).
 *            Direct subscription to /queue/** is blocked so nobody can listen to another user's queue.
 * SEND:      allowed only to /app/** (our @MessageMapping handlers), never straight to the broker.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public StompAuthChannelInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            String token = JwtService.stripBearer(accessor.getFirstNativeHeader("Authorization"));
            String username = jwtService.validate(token)
                    .map(Claims::getSubject)
                    .filter(userRepository::existsByUsername)
                    .orElseThrow(() -> new MessageDeliveryException("Unauthorized: invalid or missing token"));
            accessor.setUser(JwtAuthFilter.authenticationFor(username));
            return message;
        }

        if (command == StompCommand.SUBSCRIBE || command == StompCommand.SEND) {
            if (accessor.getUser() == null) {
                throw new MessageDeliveryException("Unauthorized");
            }
            String destination = accessor.getDestination();
            if (destination == null) {
                throw new MessageDeliveryException("Destination required");
            }
            boolean allowed = command == StompCommand.SUBSCRIBE
                    ? destination.startsWith("/topic/") || destination.startsWith("/user/queue/")
                    : destination.startsWith("/app/");
            if (!allowed) {
                throw new MessageDeliveryException("Forbidden destination: " + destination);
            }
        }
        return message;
    }
}
