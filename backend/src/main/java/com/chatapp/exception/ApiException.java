package com.chatapp.exception;

import org.springframework.http.HttpStatus;

/**
 * Business error with an HTTP status, e.g. 404 "User not found" or 409 "Username already taken".
 * Thrown from services and turned into a JSON ErrorResponse by GlobalExceptionHandler.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String field;

    public ApiException(HttpStatus status, String message) {
        this(status, message, null);
    }

    /** @param field optional form field the error belongs to (shown next to that input in the UI) */
    public ApiException(HttpStatus status, String message, String field) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public HttpStatus getStatus() { return status; }
    public String getField() { return field; }

    public static ApiException notFound(String message) { return new ApiException(HttpStatus.NOT_FOUND, message); }
    public static ApiException badRequest(String message) { return new ApiException(HttpStatus.BAD_REQUEST, message); }
    public static ApiException forbidden(String message) { return new ApiException(HttpStatus.FORBIDDEN, message); }
    public static ApiException conflict(String message, String field) { return new ApiException(HttpStatus.CONFLICT, message, field); }
    public static ApiException unauthorized(String message) { return new ApiException(HttpStatus.UNAUTHORIZED, message); }
}
