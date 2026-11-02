package com.chatapp.dto;

/** Payload sent by the browser to /app/private.send. Validated in the service. */
public record PrivateSendRequest(String to, String content) {
}
