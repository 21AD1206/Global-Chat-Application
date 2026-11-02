package com.chatapp.service;

import com.chatapp.dto.AuthResponse;
import com.chatapp.dto.LoginRequest;
import com.chatapp.dto.RegisterRequest;
import com.chatapp.exception.ApiException;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Registration, login and logout. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Creates the account and logs the user in straight away.
     * Username and email are normalised to lower case, so "Alice" and "alice" count as the same username.
     */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String username = req.username().trim().toLowerCase(Locale.ROOT);
        String email = req.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("Username is already taken", "username");
        }
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Email is already registered", "email");
        }

        User user = new User();
        user.setName(req.name().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Two people registered the same name at the same moment: the DB unique constraint caught it.
            throw ApiException.conflict("Username or email is already registered", "username");
        }
        return new AuthResponse(jwtService.generateToken(user.getUsername()), userService.toSelfDto(user));
    }

    /** Accepts username OR email. The error message is deliberately the same for "no such user" and "wrong password". */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String id = req.usernameOrEmail().trim().toLowerCase(Locale.ROOT);
        User user = (id.contains("@") ? userRepository.findByEmail(id) : userRepository.findByUsername(id))
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid username/email or password"));
        return new AuthResponse(jwtService.generateToken(user.getUsername()), userService.toSelfDto(user));
    }

    public void logout(String token) {
        jwtService.revoke(token);
    }
}
