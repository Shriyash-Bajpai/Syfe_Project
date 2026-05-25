package com.finance.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generic message response DTO for simple success/error messages.
 */
@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
}
