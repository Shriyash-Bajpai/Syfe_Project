package com.finance.manager.exception;

/**
 * Thrown when a user attempts to access or modify a resource they don't own.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
