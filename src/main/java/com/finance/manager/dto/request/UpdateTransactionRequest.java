package com.finance.manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing transaction.
 * Date cannot be modified per business rules.
 */
@Data
public class UpdateTransactionRequest {

    @DecimalMin(value = "0.01", message = "Amount must be a positive value")
    private BigDecimal amount;

    private String category;

    private String description;
}
