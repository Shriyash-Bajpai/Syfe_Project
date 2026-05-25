package com.finance.manager.service;

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
import com.finance.manager.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Category salaryCategory;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();

        salaryCategory = Category.builder()
                .id(1L).name("Salary").type(TransactionType.INCOME).isCustom(false).build();

        transaction = Transaction.builder()
                .id(1L).amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(1))
                .category(salaryCategory)
                .description("Test salary")
                .user(user)
                .build();
    }

    @Test
    void createTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");
        request.setDescription("January Salary");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("Salary")).thenReturn(Optional.of(salaryCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction("test@example.com", request);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals(TransactionType.INCOME, response.getType());
    }

    @Test
    void createTransaction_FutureDate_ThrowsBadRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDate(LocalDate.now().plusDays(1));
        request.setCategory("Salary");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> transactionService.createTransaction("test@example.com", request));
    }

    @Test
    void createTransaction_InvalidCategory_ThrowsNotFound() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("NonExistent");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("NonExistent")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("NonExistent", user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransaction("test@example.com", request));
    }

    @Test
    void getTransactions_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserWithFilters(user, null, null, null))
                .thenReturn(List.of(transaction));

        TransactionListResponse response = transactionService.getTransactions("test@example.com", null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
    }

    @Test
    void updateTransaction_Success() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("6000.00"));
        request.setDescription("Updated salary");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.updateTransaction("test@example.com", 1L, request);

        assertNotNull(response);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_NotFound_ThrowsException() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("1000.00"));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction("test@example.com", 99L, request));
    }

    @Test
    void deleteTransaction_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> transactionService.deleteTransaction("test@example.com", 1L));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_NotFound_ThrowsException() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction("test@example.com", 99L));
    }
}
