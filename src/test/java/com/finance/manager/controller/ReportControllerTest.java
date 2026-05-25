package com.finance.manager.controller;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;
import com.finance.manager.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@WithMockUser(username = "test@example.com")
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ReportService reportService;

    @Test
    void getMonthlyReport_Returns200() throws Exception {
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .month(1).year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("3000.00")))
                .totalExpenses(Map.of("Food", new BigDecimal("500.00")))
                .netSavings(new BigDecimal("2500.00"))
                .build();

        when(reportService.getMonthlyReport("test@example.com", 2024, 1)).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(500.00))
                .andExpect(jsonPath("$.netSavings").value(2500.00));
    }

    @Test
    void getMonthlyReport_EmptyData_Returns200() throws Exception {
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .month(6).year(2024)
                .totalIncome(Map.of())
                .totalExpenses(Map.of())
                .netSavings(BigDecimal.ZERO)
                .build();

        when(reportService.getMonthlyReport("test@example.com", 2024, 6)).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2024/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netSavings").value(0));
    }

    @Test
    void getYearlyReport_Returns200() throws Exception {
        YearlyReportResponse response = YearlyReportResponse.builder()
                .year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("36000.00")))
                .totalExpenses(Map.of("Food", new BigDecimal("4800.00"), "Rent", new BigDecimal("14400.00")))
                .netSavings(new BigDecimal("16800.00"))
                .build();

        when(reportService.getYearlyReport("test@example.com", 2024)).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(36000.00))
                .andExpect(jsonPath("$.netSavings").value(16800.00));
    }

    @Test
    void getYearlyReport_EmptyData_Returns200() throws Exception {
        YearlyReportResponse response = YearlyReportResponse.builder()
                .year(2023)
                .totalIncome(Map.of())
                .totalExpenses(Map.of())
                .netSavings(BigDecimal.ZERO)
                .build();

        when(reportService.getYearlyReport("test@example.com", 2023)).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2023"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.netSavings").value(0));
    }
}
