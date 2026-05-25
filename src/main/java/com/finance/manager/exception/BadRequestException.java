package com.finance.manager.exception;

/**
 * Thrown when the request contains invalid business logic violations.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
