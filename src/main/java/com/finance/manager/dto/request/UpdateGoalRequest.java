package com.finance.manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating a savings goal.
 */
@Data
public class UpdateGoalRequest {

    @DecimalMin(value = "0.01", message = "Target amount must be a positive value")
    private BigDecimal targetAmount;

    private LocalDate targetDate;
}
