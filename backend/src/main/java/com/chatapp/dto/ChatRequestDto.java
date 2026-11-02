package com.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatRequestDto(
        Long id,
        UserDto sender,
        UserDto recipient,
        String status,
        Instant createdAt,
        Instant respondedAt
) {
}
