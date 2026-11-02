package com.chatapp.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /api/auth/login. The user can log in with either username or email. */
public record LoginRequest(
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,

        @NotBlank(message = "Password is required")
        String password
) {
}
