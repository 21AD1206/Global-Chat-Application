package com.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Public view of a user. Null fields are omitted from JSON:
 * - email is only filled in when a user looks at their own profile;
 * - relation / requestId are only filled in on the profile endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
        Long id,
        String name,
        String username,
        String email,
        String bio,
        Instant createdAt,
        Instant lastSeen,
        boolean online,
        boolean live,
        String relation,
        Long requestId
) {
}
