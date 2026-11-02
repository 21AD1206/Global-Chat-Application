package com.chatapp.exception;

import com.chatapp.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Errors raised while handling a WebSocket (STOMP) message, e.g. sending an empty message or
 * messaging a user without an accepted request. The error is sent back only to the session that
 * caused it, on /user/queue/errors, and the UI shows it as a toast.
 */
@ControllerAdvice
public class WebSocketExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketExceptionHandler.class);

    @MessageExceptionHandler(ApiException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleApi(ApiException ex) {
        return ErrorResponse.of(ex.getStatus().value(), ex.getStatus().getReasonPhrase(), ex.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleOther(Exception ex) {
        log.error("Unexpected WebSocket error", ex);
        return ErrorResponse.of(500, "Internal Server Error", "Could not process the message");
    }
}
