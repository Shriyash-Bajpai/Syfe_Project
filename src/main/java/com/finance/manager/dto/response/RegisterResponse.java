package com.finance.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for successful user registration.
 */
@Data
@AllArgsConstructor
public class RegisterResponse {
    private String message;
    private Long userId;
}
