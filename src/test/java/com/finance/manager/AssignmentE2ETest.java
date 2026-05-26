package com.finance.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Maven version of the assignment-provided financial_manager_tests.sh E2E suite.
 * Each DynamicTest corresponds to one api_test call in that shell script.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssignmentE2ETest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final String runId = String.valueOf(System.currentTimeMillis());
    private final String user1Email = "john.doe." + runId + "@example.com";
    private final String user2Email = "jane.doe." + runId + "@example.com";
    private final String customIncomeCategory = "Freelance_" + runId;
    private final String customExpenseCategory = "GymMembership_" + runId;
    private final String sideBusinessCategory = "SideBusiness_" + runId;

    private MockHttpSession user1Session;
    private MockHttpSession user2Session;
    private JsonNode lastJson;
    private String lastBody;

    private long transactionId1;
    private long transactionId2;
    private long transactionId3;
    private long transactionId4;
    private long goalId1;
    private long goalId2;
    private long febIncomeId;
    private long febExpenseId;

    @TestFactory
    List<DynamicTest> assignmentE2ETests() {
        List<DynamicTest> tests = new ArrayList<>();

        add(tests, "Register first user", () -> post("/auth/register", body(
                "username", user1Email,
                "password", "securePassword123",
                "fullName", "John Doe",
                "phoneNumber", "+1234567890"), null, "2xx"));
        add(tests, "Register second user", () -> post("/auth/register", body(
                "username", user2Email,
                "password", "anotherPassword123",
                "fullName", "Jane Doe",
                "phoneNumber", "+0987654321"), null, "2xx"));
        add(tests, "Attempt duplicate registration", () -> post("/auth/register", body(
                "username", user1Email,
                "password", "differentPassword",
                "fullName", "John Duplicate",
                "phoneNumber", "+1111111111"), null, "4xx"));
        add(tests, "Register with missing username", () -> post("/auth/register", body(
                "password", "password123",
                "fullName", "Missing Username",
                "phoneNumber", "+1234567890"), null, "4xx"));
        add(tests, "Register with missing password", () -> post("/auth/register", body(
                "username", "missing.password." + runId + "@example.com",
                "fullName", "Missing Password",
                "phoneNumber", "+1234567890"), null, "4xx"));

        add(tests, "Login with valid credentials", () -> user1Session = login(user1Email, "securePassword123", "2xx"));
        add(tests, "Login with invalid password", () -> login(user1Email, "wrongPassword", "4xx"));
        add(tests, "Login with non-existent user", () -> login("nonexistent." + runId + "@example.com", "password123", "4xx"));
        add(tests, "Access protected endpoint with valid session", () -> get("/categories", user1Session, "2xx"));
        add(tests, "Access protected endpoint without session", () -> get("/categories", null, "4xx"));
        add(tests, "Logout successfully", () -> {
            post("/auth/logout", null, user1Session, "2xx");
            user1Session = null;
        });
        add(tests, "Access protected endpoint after logout", () -> get("/categories", user1Session, "4xx"));

        add(tests, "Re-login for transaction tests", () -> user1Session = login(user1Email, "securePassword123", "2xx"));
        add(tests, "Create income transaction (Salary)", () -> {
            post("/transactions", tx("5000.00", "2024-01-15", "Salary", "January Salary"), user1Session, "2xx");
            transactionId1 = id();
            assertDecimal("amount", "5000.00");
            assertText("type", "INCOME");
        });
        add(tests, "Create expense transaction (Rent)", () -> {
            post("/transactions", tx("1200.00", "2024-01-16", "Rent", "Monthly rent payment"), user1Session, "2xx");
            transactionId2 = id();
            assertDecimal("amount", "1200.00");
            assertText("type", "EXPENSE");
        });
        add(tests, "Create expense transaction (Food)", () -> {
            post("/transactions", tx("400.00", "2024-01-17", "Food", "Groceries"), user1Session, "2xx");
            transactionId3 = id();
        });
        add(tests, "Create transaction with invalid category", () -> post("/transactions",
                tx("100.00", "2024-01-18", "NonExistentCategory", "Invalid category test"), user1Session, "4xx"));
        add(tests, "Create transaction with future date", () -> post("/transactions",
                tx("100.00", "2026-12-31", "Food", "Future transaction"), user1Session, "4xx"));
        add(tests, "Create transaction with negative amount", () -> post("/transactions",
                tx("-100.00", "2024-01-17", "Food", "Negative amount"), user1Session, "4xx"));
        add(tests, "Create transaction with zero amount", () -> post("/transactions",
                tx("0", "2024-01-17", "Food", "Zero amount"), user1Session, "4xx"));
        add(tests, "Create transaction with invalid date format", () -> rawPost("/transactions",
                "{\"amount\":100.00,\"date\":\"01-17-2024\",\"category\":\"Food\",\"description\":\"Invalid date format\"}",
                user1Session, "4xx"));
        add(tests, "Get all transactions", () -> {
            get("/transactions", user1Session, "2xx");
            assertIdCount(3);
        });
        add(tests, "Filter transactions by category", () -> {
            get("/transactions?category=Salary", user1Session, "2xx");
            assertContains("Salary");
            assertIdCount(1);
        });
        add(tests, "Filter transactions by date range", () -> {
            get("/transactions?startDate=2024-01-15&endDate=2024-01-16", user1Session, "2xx");
            assertIdCount(2);
        });
        add(tests, "Attempt to update transaction date (should ignore date field)", () -> {
            put("/transactions/" + transactionId1, "{\"date\":\"2024-01-20\"}", user1Session, "2xx");
            assertText("date", "2024-01-15");
        });
        add(tests, "Update transaction amount and description (no date)", () -> {
            put("/transactions/" + transactionId1, "{\"amount\":5500.00,\"description\":\"Updated January Salary with bonus\"}",
                    user1Session, "2xx");
            assertText("date", "2024-01-15");
            assertDecimal("amount", "5500.00");
        });
        add(tests, "Update with date field included (should ignore date)", () -> {
            put("/transactions/" + transactionId3, "{\"amount\":450.00,\"date\":\"2024-01-18\",\"description\":\"Trying to change date\"}",
                    user1Session, "2xx");
            assertText("date", "2024-01-17");
            assertDecimal("amount", "450.00");
            assertText("description", "Trying to change date");
        });
        add(tests, "Update non-existent transaction", () -> put("/transactions/999999", "{\"amount\":1000.00}", user1Session, "4xx"));
        add(tests, "Delete transaction", () -> delete("/transactions/" + transactionId2, user1Session, "2xx"));
        add(tests, "Delete non-existent transaction", () -> delete("/transactions/999999", user1Session, "4xx"));

        add(tests, "Get all categories (default + custom)", () -> {
            get("/categories", user1Session, "2xx");
            assertContains("Salary");
            assertContains("Food");
            assertContains("Rent");
        });
        add(tests, "Create custom income category", () -> {
            post("/categories", body("name", customIncomeCategory, "type", "INCOME"), user1Session, "2xx");
            assertTrue(lastJson.path("custom").asBoolean());
        });
        add(tests, "Create custom expense category", () ->
                post("/categories", body("name", customExpenseCategory, "type", "EXPENSE"), user1Session, "2xx"));
        add(tests, "Attempt duplicate category creation", () ->
                post("/categories", body("name", customIncomeCategory, "type", "INCOME"), user1Session, "4xx"));
        add(tests, "Create category with invalid type", () ->
                rawPost("/categories", "{\"name\":\"InvalidType\",\"type\":\"INVALID\"}", user1Session, "4xx"));
        add(tests, "Create transaction with custom category", () -> {
            post("/transactions", tx("1500.00", "2024-01-18", customIncomeCategory, "Client project payment"),
                    user1Session, "2xx");
            transactionId4 = id();
        });
        add(tests, "Try to delete category in use", () -> delete("/categories/" + customIncomeCategory, user1Session, "4xx"));
        add(tests, "Delete unused custom category", () -> delete("/categories/" + customExpenseCategory, user1Session, "2xx"));
        add(tests, "Try to delete default category", () -> delete("/categories/Food", user1Session, "4xx"));

        add(tests, "Create goal without start date (should default to today)", () -> {
            post("/goals", "{\"goalName\":\"Emergency Fund No Start Date\",\"targetAmount\":10000.00,\"targetDate\":\"2027-01-01\"}",
                    user1Session, "2xx");
            assertDefaultStartDate();
        });
        add(tests, "Create emergency fund goal with explicit start date", () -> {
            post("/goals", goal("Emergency Fund", "10000.00", "2027-01-01", "2024-01-01"), user1Session, "2xx");
            goalId1 = id();
            assertDecimal("currentProgress", "6550.00");
            assertDecimal("progressPercentage", "65.5");
            assertDecimal("remainingAmount", "3450.00");
        });
        add(tests, "Create vacation fund goal", () -> {
            post("/goals", goal("Vacation Fund", "5000.00", "2027-12-01", "2024-02-01"), user1Session, "2xx");
            goalId2 = id();
            assertDecimal("currentProgress", "0");
            assertDecimal("progressPercentage", "0.0");
        });
        add(tests, "Create goal with past target date", () -> post("/goals",
                goal("Invalid Goal", "5000.00", "2023-01-01", null), user1Session, "4xx"));
        add(tests, "Create goal with negative amount", () -> post("/goals",
                goal("Negative Goal", "-1000.00", "2026-01-01", null), user1Session, "4xx"));
        add(tests, "Create goal with zero amount", () -> post("/goals",
                goal("Zero Goal", "0", "2026-01-01", null), user1Session, "4xx"));
        add(tests, "Create goal with start date after target date", () -> post("/goals",
                goal("Invalid Dates Goal", "5000.00", "2026-01-01", "2027-01-01"), user1Session, "4xx"));
        add(tests, "Add income after second goal start date", () -> {
            post("/transactions", tx("3000.00", "2024-02-15", "Salary", "February bonus"), user1Session, "2xx");
            febIncomeId = id();
        });
        add(tests, "Add expense after second goal start date", () -> {
            post("/transactions", tx("500.00", "2024-02-20", "Food", "February groceries"), user1Session, "2xx");
            febExpenseId = id();
        });
        add(tests, "Get vacation fund goal with updated progress", () -> {
            get("/goals/" + goalId2, user1Session, "2xx");
            assertDecimal("currentProgress", "2500.00");
            assertDecimal("progressPercentage", "50.0");
            assertDecimal("remainingAmount", "2500.00");
        });
        add(tests, "Get all goals with progress", () -> {
            get("/goals", user1Session, "2xx");
            assertIdCount(3);
        });
        add(tests, "Update goal target amount", () -> {
            put("/goals/" + goalId1, "{\"targetAmount\":15000.00}", user1Session, "2xx");
            assertDecimal("progressPercentage", "60.33");
            assertDecimal("remainingAmount", "5950.00");
        });
        add(tests, "Update goal target date", () -> put("/goals/" + goalId1, "{\"targetDate\":\"2028-01-01\"}", user1Session, "2xx"));
        add(tests, "Update non-existent goal", () -> put("/goals/999999", "{\"targetAmount\":5000.00}", user1Session, "4xx"));

        add(tests, "Get vacation fund goal before deletion", () -> {
            get("/goals/" + goalId2, user1Session, "2xx");
            assertDecimal("currentProgress", "2500.00");
        });
        add(tests, "Get February 2024 report before deletion", () -> {
            get("/reports/monthly/2024/2", user1Session, "2xx");
            assertDecimal("netSavings", "2500.00");
        });
        add(tests, "Delete February expense transaction", () -> delete("/transactions/" + febExpenseId, user1Session, "2xx"));
        add(tests, "Get vacation fund goal after expense deletion", () -> {
            get("/goals/" + goalId2, user1Session, "2xx");
            assertDecimal("currentProgress", "3000.00");
            assertDecimal("progressPercentage", "60.0");
        });
        add(tests, "Get February 2024 report after deletion", () -> {
            get("/reports/monthly/2024/2", user1Session, "2xx");
            assertDecimal("netSavings", "3000.00");
        });
        add(tests, "Delete February income transaction", () -> delete("/transactions/" + febIncomeId, user1Session, "2xx"));
        add(tests, "Get vacation fund goal after income deletion", () -> {
            get("/goals/" + goalId2, user1Session, "2xx");
            assertDecimal("currentProgress", "0");
            assertDecimal("progressPercentage", "0.0");
        });
        add(tests, "Get February 2024 report after all deletions", () -> {
            get("/reports/monthly/2024/2", user1Session, "2xx");
            assertDecimal("netSavings", "0");
        });
        add(tests, "Delete vacation fund goal", () -> delete("/goals/" + goalId2, user1Session, "2xx"));
        add(tests, "Delete non-existent goal", () -> delete("/goals/999999", user1Session, "4xx"));

        add(tests, "Generate January 2024 monthly report", () -> {
            get("/reports/monthly/2024/1", user1Session, "2xx");
            assertDecimal("netSavings", "6550.00");
            assertContains(customIncomeCategory);
            assertContains("1500.00");
        });
        add(tests, "Generate report for month with no data", () -> {
            get("/reports/monthly/2024/12", user1Session, "2xx");
            assertDecimal("netSavings", "0");
        });
        add(tests, "Generate report for invalid month", () -> get("/reports/monthly/2024/13", user1Session, "4xx"));
        add(tests, "Generate report for month 0", () -> get("/reports/monthly/2024/0", user1Session, "4xx"));
        add(tests, "Generate 2024 yearly report", () -> {
            get("/reports/yearly/2024", user1Session, "2xx");
            assertDecimal("netSavings", "6550.00");
        });
        add(tests, "Generate report for year with no data", () -> {
            get("/reports/yearly/2023", user1Session, "2xx");
            assertDecimal("netSavings", "0");
        });

        add(tests, "Login as second user", () -> user2Session = login(user2Email, "anotherPassword123", "2xx"));
        add(tests, "Second user sees only default categories", () -> {
            get("/categories", user2Session, "2xx");
            assertFalse(lastBody.contains(customIncomeCategory));
        });
        add(tests, "Second user has no transactions initially", () -> {
            get("/transactions", user2Session, "2xx");
            assertEquals(0, lastJson.path("transactions").size());
        });
        add(tests, "Second user has no goals initially", () -> {
            get("/goals", user2Session, "2xx");
            assertEquals(0, lastJson.path("goals").size());
        });
        add(tests, "Second user cannot update first user's transaction", () ->
                put("/transactions/" + transactionId1, "{\"amount\":1000.00}", user2Session, "4xx"));
        add(tests, "Second user cannot delete first user's goal", () -> delete("/goals/" + goalId1, user2Session, "4xx"));
        add(tests, "Second user creates own transaction", () -> post("/transactions",
                tx("3000.00", "2024-01-20", "Salary", "Jane January Salary"), user2Session, "2xx"));
        add(tests, "Second user creates own goal", () -> {
            post("/goals", "{\"goalName\":\"Jane Emergency Fund\",\"targetAmount\":8000.00,\"targetDate\":\"2027-12-01\"}",
                    user2Session, "2xx");
            assertDefaultStartDate();
        });
        add(tests, "First user still sees only own data", () -> {
            get("/transactions", user1Session, "2xx");
            assertFalse(lastBody.contains("Jane January Salary"));
            assertIdCount(3);
        });

        add(tests, "First user creates custom category", () ->
                post("/categories", body("name", sideBusinessCategory, "type", "INCOME"), user1Session, "2xx"));
        add(tests, "Add side business income", () -> post("/transactions",
                tx("800.00", "2024-01-25", sideBusinessCategory, "Consulting work"), user1Session, "2xx"));
        add(tests, "Check updated goal progress after new transaction", () -> {
            get("/goals/" + goalId1, user1Session, "2xx");
            assertDecimal("currentProgress", "7350.00");
        });
        add(tests, "Generate updated monthly report", () -> {
            get("/reports/monthly/2024/1", user1Session, "2xx");
            assertDecimal("netSavings", "7350.00");
            assertContains(sideBusinessCategory);
            assertContains("800.00");
        });
        add(tests, "Generate updated yearly report", () -> {
            get("/reports/yearly/2024", user1Session, "2xx");
            assertDecimal("netSavings", "7350.00");
        });
        add(tests, "Logout first user", () -> {
            post("/auth/logout", null, user1Session, "2xx");
            user1Session = null;
        });
        add(tests, "Logout second user", () -> {
            post("/auth/logout", null, user2Session, "2xx");
            user2Session = null;
        });
        add(tests, "Verify sessions invalidated", () -> get("/categories", user1Session, "4xx"));

        assertEquals(86, tests.size(), "The assignment shell script contains 86 api_test calls");
        return tests;
    }

    private void add(List<DynamicTest> tests, String name, ThrowingRunnable runnable) {
        tests.add(dynamicTest(name, runnable::run));
    }

    private MockHttpSession login(String username, String password, String expectedRange) throws Exception {
        MvcResult result = post("/auth/login", body("username", username, "password", password), null, expectedRange);
        return is2xx(result) ? (MockHttpSession) result.getRequest().getSession(false) : null;
    }

    private MvcResult get(String endpoint, MockHttpSession session, String expectedRange) throws Exception {
        return perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api" + endpoint),
                session, expectedRange);
    }

    private MvcResult post(String endpoint, String json, MockHttpSession session, String expectedRange) throws Exception {
        return rawPost(endpoint, json, session, expectedRange);
    }

    private MvcResult rawPost(String endpoint, String json, MockHttpSession session, String expectedRange) throws Exception {
        MockHttpServletRequestBuilder builder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api" + endpoint);
        if (json != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return perform(builder, session, expectedRange);
    }

    private MvcResult put(String endpoint, String json, MockHttpSession session, String expectedRange) throws Exception {
        return perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api" + endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json), session, expectedRange);
    }

    private MvcResult delete(String endpoint, MockHttpSession session, String expectedRange) throws Exception {
        return perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api" + endpoint),
                session, expectedRange);
    }

    private MvcResult perform(MockHttpServletRequestBuilder builder, MockHttpSession session, String expectedRange) throws Exception {
        if (session != null) {
            builder.session(session);
        }
        MvcResult result = mockMvc.perform(builder).andReturn();
        int status = result.getResponse().getStatus();
        lastBody = result.getResponse().getContentAsString();
        lastJson = lastBody == null || lastBody.isBlank() ? mapper.createObjectNode() : mapper.readTree(lastBody);
        assertStatusRange(status, expectedRange);
        return result;
    }

    private void assertStatusRange(int status, String expectedRange) {
        switch (expectedRange) {
            case "2xx" -> assertTrue(status >= 200 && status < 300, "Expected 2xx but got " + status + ": " + lastBody);
            case "4xx" -> assertTrue(status >= 400 && status < 500, "Expected 4xx but got " + status + ": " + lastBody);
            default -> fail("Unsupported expected range: " + expectedRange);
        }
    }

    private boolean is2xx(MvcResult result) {
        int status = result.getResponse().getStatus();
        return status >= 200 && status < 300;
    }

    private long id() {
        assertTrue(lastJson.has("id"), "Response should contain id: " + lastBody);
        return lastJson.path("id").asLong();
    }

    private void assertText(String key, String expected) {
        assertEquals(expected, lastJson.path(key).asText(), "Unexpected " + key + " in " + lastBody);
    }

    private void assertDecimal(String key, String expected) {
        BigDecimal actual = lastJson.path(key).decimalValue();
        assertEquals(0, actual.compareTo(new BigDecimal(expected)), "Unexpected " + key + " in " + lastBody);
    }

    private void assertContains(String expected) {
        assertTrue(lastBody.contains(expected), "Response should contain '" + expected + "': " + lastBody);
    }

    private void assertIdCount(int expected) {
        int count = lastBody.split("\"id\"", -1).length - 1;
        assertEquals(expected, count, "Unexpected id count in " + lastBody);
    }

    private void assertDefaultStartDate() {
        String actual = lastJson.path("startDate").asText();
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();
        assertTrue(actual.equals(today) || actual.equals(yesterday),
                "Expected startDate to be today or yesterday, got " + actual);
    }

    private String tx(String amount, String date, String category, String description) {
        return "{\"amount\":" + amount + ",\"date\":\"" + date + "\",\"category\":\"" + category
                + "\",\"description\":\"" + description + "\"}";
    }

    private String goal(String name, String amount, String targetDate, String startDate) {
        String json = "{\"goalName\":\"" + name + "\",\"targetAmount\":" + amount + ",\"targetDate\":\"" + targetDate + "\"";
        if (startDate != null) {
            json += ",\"startDate\":\"" + startDate + "\"";
        }
        return json + "}";
    }

    private String body(String... keyValues) throws Exception {
        var node = mapper.createObjectNode();
        for (int i = 0; i < keyValues.length; i += 2) {
            node.put(keyValues[i], keyValues[i + 1]);
        }
        return mapper.writeValueAsString(node);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
