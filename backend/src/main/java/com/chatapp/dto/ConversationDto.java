package com.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/** One entry in the "Private chats" list: the other person and the latest message. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConversationDto(
        UserDto partner,
        MessageDto lastMessage,
        Instant connectedSince
) {
}
