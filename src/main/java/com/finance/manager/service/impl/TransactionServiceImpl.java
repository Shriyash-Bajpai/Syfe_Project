package com.finance.manager.service.impl;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.TransactionListResponse;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.enums.TransactionType;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TransactionService for managing financial transactions.
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(String username, TransactionRequest request) {
        User user = getUser(username);

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be a future date");
        }

        Category category = resolveCategory(request.getCategory(), user);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .description(request.getDescription())
                .user(user)
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(
            String username,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            TransactionType type) {
        return getTransactions(username, startDate, endDate, categoryId, null, type);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(
            String username,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            String categoryName,
            TransactionType type) {
        User user = getUser(username);

        Category categoryFilter = null;
        if (categoryId != null) {
            categoryFilter = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        } else if (categoryName != null && !categoryName.isBlank()) {
            categoryFilter = resolveCategory(categoryName, user);
        }

        List<Transaction> transactions = transactionRepository.findByUserWithFilters(
                user, startDate, endDate, categoryFilter, type);

        List<TransactionResponse> responses = transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new TransactionListResponse(responses);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(String username, Long id, UpdateTransactionRequest request) {
        User user = getUser(username);

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            Category category = resolveCategory(request.getCategory(), user);
            transaction.setCategory(category);
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void deleteTransaction(String username, Long id) {
        User user = getUser(username);

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        transactionRepository.delete(transaction);
    }

    /**
     * Resolves a category name to a Category entity accessible by the user.
     * Checks default categories first, then user's custom categories.
     */
    private Category resolveCategory(String categoryName, User user) {
        // Try default category first
        Category category = categoryRepository.findByNameAndIsCustomFalse(categoryName).orElse(null);
        if (category != null) {
            return category;
        }
        // Try user's custom category
        category = categoryRepository.findByNameAndUser(categoryName, user).orElse(null);
        if (category != null) {
            return category;
        }
        throw new ResourceNotFoundException("Category not found: " + categoryName);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Maps a Transaction entity to its response DTO.
     */
    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .category(transaction.getCategory().getName())
                .description(transaction.getDescription())
                .type(transaction.getCategory().getType())
                .build();
    }
}
