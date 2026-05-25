package com.finance.manager.exception;

/**
 * Thrown when a resource with conflicting unique constraints already exists.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
