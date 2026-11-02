package com.chatapp.model;

/** Life cycle of a private chat request. */
public enum RequestStatus {
    /** Sent, waiting for the recipient to answer. */
    PENDING,
    /** Recipient accepted: both users may now exchange private messages. */
    ACCEPTED,
    /** Recipient rejected the request. The sender may try again later. */
    REJECTED,
    /** Sender withdrew the request before it was answered. */
    CANCELLED
}
