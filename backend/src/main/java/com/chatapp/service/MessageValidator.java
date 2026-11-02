package com.chatapp.service;

import com.chatapp.exception.ApiException;

/** Shared validation for chat message text (global and private). */
public final class MessageValidator {

    public static final int MAX_LENGTH = 1000;

    private MessageValidator() {
    }

    /** Returns the trimmed content, or throws 400 if it is empty or too long. */
    public static String clean(String content) {
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw ApiException.badRequest("Message cannot be empty");
        }
        if (text.length() > MAX_LENGTH) {
            throw ApiException.badRequest("Message is too long (max " + MAX_LENGTH + " characters)");
        }
        return text;
    }
}
