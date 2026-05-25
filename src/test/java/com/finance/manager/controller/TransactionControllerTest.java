package com.finance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.response.TransactionListResponse;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.enums.TransactionType;
import com.finance.manager.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@WithMockUser(username = "test@example.com")
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private TransactionService transactionService;

    @Test
    void createTransaction_ValidRequest_Returns201() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");
        request.setDescription("January Salary");

        TransactionResponse response = TransactionResponse.builder()
                .id(1L).amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(1))
                .category("Salary").type(TransactionType.INCOME)
                .build();

        when(transactionService.createTransaction(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    void getTransactions_Returns200() throws Exception {
        TransactionResponse txResponse = TransactionResponse.builder()
                .id(1L).amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(1))
                .category("Salary").type(TransactionType.INCOME).build();

        when(transactionService.getTransactions(any(), any(), any(), any()))
                .thenReturn(new TransactionListResponse(List.of(txResponse)));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].id").value(1));
    }

    @Test
    void deleteTransaction_Returns200() throws Exception {
        mockMvc.perform(delete("/api/transactions/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }

    @Test
    void createTransaction_MissingAmount_Returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
