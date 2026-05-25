package com.finance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.GoalListResponse;
import com.finance.manager.dto.response.GoalResponse;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.service.SavingsGoalService;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavingsGoalController.class)
@WithMockUser(username = "test@example.com")
class SavingsGoalControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private SavingsGoalService savingsGoalService;

    private ObjectMapper objectMapper;
    private GoalResponse sampleGoal;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleGoal = GoalResponse.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now())
                .currentProgress(BigDecimal.ZERO)
                .progressPercentage(0.0)
                .remainingAmount(new BigDecimal("5000.00"))
                .build();
    }

    @Test
    void createGoal_ValidRequest_Returns201() throws Exception {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setTargetDate(LocalDate.now().plusYears(1));

        when(savingsGoalService.createGoal(eq("test@example.com"), any())).thenReturn(sampleGoal);

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.targetAmount").value(5000.00))
                .andExpect(jsonPath("$.progressPercentage").value(0.0));
    }

    @Test
    void createGoal_MissingGoalName_Returns400() throws Exception {
        GoalRequest request = new GoalRequest();
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setTargetDate(LocalDate.now().plusYears(1));

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGoal_PastTargetDate_Returns400() throws Exception {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Old Goal");
        request.setTargetAmount(new BigDecimal("1000.00"));
        request.setTargetDate(LocalDate.now().plusYears(1));

        when(savingsGoalService.createGoal(any(), any()))
                .thenThrow(new BadRequestException("Target date must be a future date"));

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllGoals_Returns200() throws Exception {
        when(savingsGoalService.getAllGoals("test@example.com"))
                .thenReturn(new GoalListResponse(List.of(sampleGoal)));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals").isArray())
                .andExpect(jsonPath("$.goals.length()").value(1))
                .andExpect(jsonPath("$.goals[0].goalName").value("Emergency Fund"));
    }

    @Test
    void getGoal_ValidId_Returns200() throws Exception {
        when(savingsGoalService.getGoal("test@example.com", 1L)).thenReturn(sampleGoal);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"));
    }

    @Test
    void getGoal_NotFound_Returns404() throws Exception {
        when(savingsGoalService.getGoal("test@example.com", 99L))
                .thenThrow(new ResourceNotFoundException("Goal not found with id: 99"));

        mockMvc.perform(get("/api/goals/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGoal_OtherUser_Returns403() throws Exception {
        when(savingsGoalService.getGoal("test@example.com", 2L))
                .thenThrow(new ForbiddenException("You do not have access to this goal"));

        mockMvc.perform(get("/api/goals/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateGoal_ValidRequest_Returns200() throws Exception {
        UpdateGoalRequest request = new UpdateGoalRequest();
        request.setTargetAmount(new BigDecimal("6000.00"));
        request.setTargetDate(LocalDate.now().plusYears(2));

        GoalResponse updated = GoalResponse.builder()
                .id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("6000.00"))
                .targetDate(LocalDate.now().plusYears(2))
                .startDate(LocalDate.now())
                .currentProgress(BigDecimal.ZERO)
                .progressPercentage(0.0)
                .remainingAmount(new BigDecimal("6000.00"))
                .build();

        when(savingsGoalService.updateGoal(eq("test@example.com"), eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/goals/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(6000.00));
    }

    @Test
    void deleteGoal_Success_Returns200() throws Exception {
        doNothing().when(savingsGoalService).deleteGoal("test@example.com", 1L);

        mockMvc.perform(delete("/api/goals/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }

    @Test
    void deleteGoal_OtherUser_Returns403() throws Exception {
        doThrow(new ForbiddenException("You do not have access to this goal"))
                .when(savingsGoalService).deleteGoal("test@example.com", 2L);

        mockMvc.perform(delete("/api/goals/2").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
