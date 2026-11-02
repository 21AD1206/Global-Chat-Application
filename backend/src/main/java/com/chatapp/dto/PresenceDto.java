package com.chatapp.dto;

/** Online / LIVE state of one user, broadcast on /topic/presence. */
public record PresenceDto(String username, boolean online, boolean live) {
}
