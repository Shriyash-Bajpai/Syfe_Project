package com.finance.manager.exception;

import com.finance.manager.dto.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that maps exceptions to appropriate HTTP responses.
 * Ensures no 5xx errors occur for known scenarios.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles resource not found errors (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles conflict errors such as duplicate resource names (409).
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<MessageResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles access to resources not owned by the requesting user (403).
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<MessageResponse> handleForbiddenException(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles bad request / business logic violations (400).
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MessageResponse> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles malformed JSON, invalid date formats, and invalid enum values (400).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid request body"));
    }

    /**
     * Handles authentication failures (401).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid credentials"));
    }

    /**
     * Fallback handler for unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An unexpected error occurred: " + ex.getMessage()));
    }
}
