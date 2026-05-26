package com.finance.manager.service;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;
import com.finance.manager.entity.User;
import com.finance.manager.enums.TransactionType;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
    }

    @Test
    void getMonthlyReport_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.sumByUserAndTypeAndYearAndMonth(user, TransactionType.INCOME, 2024, 1))
                .thenReturn(List.<Object[]>of(new Object[]{"Salary", new BigDecimal("3000.00")}));
        when(transactionRepository.sumByUserAndTypeAndYearAndMonth(user, TransactionType.EXPENSE, 2024, 1))
                .thenReturn(List.<Object[]>of(new Object[]{"Food", new BigDecimal("500.00")}));

        MonthlyReportResponse response = reportService.getMonthlyReport("test@example.com", 2024, 1);

        assertNotNull(response);
        assertEquals(1, response.getMonth());
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("3000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("500.00"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("2500.00"), response.getNetSavings());
    }

    @Test
    void getMonthlyReport_NoTransactions_ReturnsZero() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.sumByUserAndTypeAndYearAndMonth(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        MonthlyReportResponse response = reportService.getMonthlyReport("test@example.com", 2024, 1);

        assertNotNull(response);
        assertTrue(response.getTotalIncome().isEmpty());
        assertTrue(response.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }

    @Test
    void getYearlyReport_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.sumByUserAndTypeAndYear(user, TransactionType.INCOME, 2024))
                .thenReturn(List.<Object[]>of(new Object[]{"Salary", new BigDecimal("36000.00")}));
        when(transactionRepository.sumByUserAndTypeAndYear(user, TransactionType.EXPENSE, 2024))
                .thenReturn(List.<Object[]>of(new Object[]{"Food", new BigDecimal("6000.00")}));

        YearlyReportResponse response = reportService.getYearlyReport("test@example.com", 2024);

        assertNotNull(response);
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("36000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("6000.00"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("30000.00"), response.getNetSavings());
    }

    @Test
    void getYearlyReport_NoTransactions_ReturnsZero() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.sumByUserAndTypeAndYear(any(), any(), anyInt()))
                .thenReturn(List.of());

        YearlyReportResponse response = reportService.getYearlyReport("test@example.com", 2024);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }
}
