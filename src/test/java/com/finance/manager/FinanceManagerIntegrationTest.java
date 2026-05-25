package com.finance.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finance.manager.dto.request.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests covering the full API lifecycle.
 * Tests run in order to simulate a real user session.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinanceManagerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static MockHttpSession session = new MockHttpSession();
    private static Long transactionId;
    private static Long goalId;

    // ─── Auth ──────────────────────────────────────────────────────────────

    @Test @Order(1)
    void register_NewUser_Returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("integration@example.com");
        req.setPassword("password123");
        req.setFullName("Integration User");
        req.setPhoneNumber("+9876543210");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test @Order(2)
    void register_DuplicateEmail_Returns409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("integration@example.com");
        req.setPassword("password123");
        req.setFullName("Duplicate");
        req.setPhoneNumber("+1111111111");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test @Order(3)
    void login_ValidCredentials_Returns200AndSetsSession() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("integration@example.com");
        req.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        session = (MockHttpSession) result.getRequest().getSession();
    }

    @Test @Order(4)
    void login_WrongPassword_Returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("integration@example.com");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ─── Categories ────────────────────────────────────────────────────────

    @Test @Order(5)
    void getCategories_AfterLogin_ReturnsDefaultCategories() throws Exception {
        mockMvc.perform(get("/api/categories").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].isCustom").value(false));
    }

    @Test @Order(6)
    void createCustomCategory_Returns201() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Freelance");
        req.setType(com.finance.manager.enums.TransactionType.INCOME);

        mockMvc.perform(post("/api/categories")
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Freelance"))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test @Order(7)
    void createDuplicateCategory_Returns409() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Freelance");
        req.setType(com.finance.manager.enums.TransactionType.INCOME);

        mockMvc.perform(post("/api/categories")
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // ─── Transactions ──────────────────────────────────────────────────────

    @Test @Order(8)
    void createTransaction_ValidIncome_Returns201() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("5000.00"));
        req.setDate(LocalDate.now().minusDays(5));
        req.setCategory("Salary");
        req.setDescription("January Salary");

        MvcResult result = mockMvc.perform(post("/api/transactions")
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Salary"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        transactionId = mapper.readTree(body).get("id").asLong();
    }

    @Test @Order(9)
    void createTransaction_FutureDate_Returns400() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("100.00"));
        req.setDate(LocalDate.now().plusDays(1));
        req.setCategory("Salary");

        mockMvc.perform(post("/api/transactions")
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(10)
    void createTransaction_InvalidCategory_Returns404() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("100.00"));
        req.setDate(LocalDate.now().minusDays(1));
        req.setCategory("NonExistentCategory");

        mockMvc.perform(post("/api/transactions")
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test @Order(11)
    void getTransactions_Returns200WithList() throws Exception {
        mockMvc.perform(get("/api/transactions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray());
    }

    @Test @Order(12)
    void updateTransaction_ValidRequest_Returns200() throws Exception {
        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setAmount(new BigDecimal("6000.00"));
        req.setDescription("Updated salary");

        mockMvc.perform(put("/api/transactions/" + transactionId)
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(6000.00));
    }

    // ─── Savings Goals ─────────────────────────────────────────────────────

    @Test @Order(13)
    void createGoal_ValidRequest_Returns201() throws Exception {
        GoalRequest req = new GoalRequest();
        req.setGoalName("Emergency Fund");
        req.setTargetAmount(new BigDecimal("10000.00"));
        req.setTargetDate(LocalDate.now().plusYears(1));
        req.setStartDate(LocalDate.now().minusDays(10));

        MvcResult result = mockMvc.perform(post("/api/goals")
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.progressPercentage").isNumber())
                .andExpect(jsonPath("$.remainingAmount").isNumber())
                .andReturn();

        goalId = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test @Order(14)
    void createGoal_PastTargetDate_Returns400() throws Exception {
        GoalRequest req = new GoalRequest();
        req.setGoalName("Old Goal");
        req.setTargetAmount(new BigDecimal("1000.00"));
        req.setTargetDate(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/goals")
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(15)
    void getAllGoals_Returns200() throws Exception {
        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals").isArray());
    }

    @Test @Order(16)
    void getGoal_ValidId_Returns200() throws Exception {
        mockMvc.perform(get("/api/goals/" + goalId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(goalId));
    }

    @Test @Order(17)
    void updateGoal_ValidRequest_Returns200() throws Exception {
        UpdateGoalRequest req = new UpdateGoalRequest();
        req.setTargetAmount(new BigDecimal("12000.00"));
        req.setTargetDate(LocalDate.now().plusYears(2));

        mockMvc.perform(put("/api/goals/" + goalId)
                        .with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(12000.00));
    }

    // ─── Reports ───────────────────────────────────────────────────────────

    @Test @Order(18)
    void getMonthlyReport_Returns200() throws Exception {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        mockMvc.perform(get("/api/reports/monthly/" + year + "/" + month).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(month))
                .andExpect(jsonPath("$.year").value(year))
                .andExpect(jsonPath("$.netSavings").isNumber());
    }

    @Test @Order(19)
    void getYearlyReport_Returns200() throws Exception {
        int year = LocalDate.now().getYear();

        mockMvc.perform(get("/api/reports/yearly/" + year).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(year))
                .andExpect(jsonPath("$.netSavings").isNumber());
    }

    // ─── Cleanup / Deletion ────────────────────────────────────────────────

    @Test @Order(20)
    void deleteTransaction_Returns200() throws Exception {
        mockMvc.perform(delete("/api/transactions/" + transactionId)
                        .with(csrf()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }

    @Test @Order(21)
    void deleteTransaction_NotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/api/transactions/99999").with(csrf()).session(session))
                .andExpect(status().isNotFound());
    }

    @Test @Order(22)
    void deleteGoal_Returns200() throws Exception {
        mockMvc.perform(delete("/api/goals/" + goalId).with(csrf()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }

    @Test @Order(23)
    void deleteCustomCategory_Returns200() throws Exception {
        mockMvc.perform(delete("/api/categories/Freelance").with(csrf()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test @Order(24)
    void deleteDefaultCategory_Returns403() throws Exception {
        mockMvc.perform(delete("/api/categories/Salary").with(csrf()).session(session))
                .andExpect(status().isForbidden());
    }

    @Test @Order(25)
    void accessProtectedEndpoint_WithoutSession_Returns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(26)
    void logout_Returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(csrf()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
