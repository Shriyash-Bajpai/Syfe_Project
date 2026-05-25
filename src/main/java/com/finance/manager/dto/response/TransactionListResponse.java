package com.finance.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Response DTO wrapping a list of transactions.
 */
@Data
@AllArgsConstructor
public class TransactionListResponse {
    private List<TransactionResponse> transactions;
}
