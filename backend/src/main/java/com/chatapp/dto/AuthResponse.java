package com.chatapp.dto;

/** Returned after successful register/login: the JWT plus the logged-in user's profile. */
public record AuthResponse(String token, UserDto user) {
}
