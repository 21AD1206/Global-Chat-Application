package com.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/** A chat message as sent to the browser. recipientUsername is null for global messages. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageDto(
        Long id,
        String senderUsername,
        String senderName,
        String recipientUsername,
        String content,
        Instant sentAt
) {
}
