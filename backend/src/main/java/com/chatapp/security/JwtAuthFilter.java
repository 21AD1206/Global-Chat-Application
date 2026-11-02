package com.chatapp.security;

import com.chatapp.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs once for every HTTP request. If the request carries a valid "Authorization: Bearer <jwt>"
 * header, the user is marked as authenticated for this request. If not, the request continues
 * unauthenticated and Spring Security rejects it with 401 when it targets a protected endpoint.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = JwtService.stripBearer(request.getHeader(HttpHeaders.AUTHORIZATION));
        jwtService.validate(token)
                .map(Claims::getSubject)
                .filter(userRepository::existsByUsername)
                .ifPresent(username -> SecurityContextHolder.getContext().setAuthentication(authenticationFor(username)));
        chain.doFilter(request, response);
    }

    /** The principal is the plain username, so Principal.getName() returns it everywhere (REST and WebSocket). */
    public static UsernamePasswordAuthenticationToken authenticationFor(String username) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
