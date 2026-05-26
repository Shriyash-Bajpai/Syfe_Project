package com.finance.manager.service.impl;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;
import com.finance.manager.entity.User;
import com.finance.manager.enums.TransactionType;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ReportService providing monthly and yearly financial reports.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(String username, int year, int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }

        User user = getUser(username);

        Map<String, BigDecimal> incomeByCategory = aggregateByCategory(
                transactionRepository.sumByUserAndTypeAndYearAndMonth(user, TransactionType.INCOME, year, month));

        Map<String, BigDecimal> expensesByCategory = aggregateByCategory(
                transactionRepository.sumByUserAndTypeAndYearAndMonth(user, TransactionType.EXPENSE, year, month));

        BigDecimal totalIncome = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expensesByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(incomeByCategory)
                .totalExpenses(expensesByCategory)
                .netSavings(netSavings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(String username, int year) {
        User user = getUser(username);

        Map<String, BigDecimal> incomeByCategory = aggregateByCategory(
                transactionRepository.sumByUserAndTypeAndYear(user, TransactionType.INCOME, year));

        Map<String, BigDecimal> expensesByCategory = aggregateByCategory(
                transactionRepository.sumByUserAndTypeAndYear(user, TransactionType.EXPENSE, year));

        BigDecimal totalIncome = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expensesByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(incomeByCategory)
                .totalExpenses(expensesByCategory)
                .netSavings(netSavings)
                .build();
    }

    /**
     * Converts a list of [categoryName, sum] object arrays into a Map.
     */
    private Map<String, BigDecimal> aggregateByCategory(List<Object[]> rows) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String categoryName = (String) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            result.put(categoryName, sum);
        }
        return result;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
