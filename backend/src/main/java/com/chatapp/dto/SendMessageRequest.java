package com.chatapp.dto;

/** Payload sent by the browser to /app/global.send. Validated in the service. */
public record SendMessageRequest(String content) {
}
