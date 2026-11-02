package com.chatapp.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /api/requests. */
public record CreateChatRequest(
        @NotBlank(message = "Recipient username is required")
        String toUsername
) {
}
