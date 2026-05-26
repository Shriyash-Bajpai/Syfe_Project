package com.finance.manager.service;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.TransactionListResponse;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.enums.TransactionType;

import java.time.LocalDate;

/**
 * Service interface for transaction management operations.
 */
public interface TransactionService {

    /**
     * Creates a new transaction for the authenticated user.
     */
    TransactionResponse createTransaction(String username, TransactionRequest request);

    /**
     * Retrieves all transactions for the authenticated user with optional filters.
     */
    TransactionListResponse getTransactions(
            String username,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            TransactionType type);

    /**
     * Updates an existing transaction. Date cannot be modified.
     */
    TransactionResponse updateTransaction(String username, Long id, UpdateTransactionRequest request);

    /**
     * Deletes a transaction belonging to the authenticated user.
     */
    void deleteTransaction(String username, Long id);
}
