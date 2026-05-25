package com.finance.manager.service;

import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.GoalListResponse;
import com.finance.manager.dto.response.GoalResponse;
import com.finance.manager.entity.SavingsGoal;
import com.finance.manager.entity.User;
import com.finance.manager.enums.TransactionType;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.SavingsGoalRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.SavingsGoalServiceImpl;
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
class SavingsGoalServiceImplTest {

    @Mock private SavingsGoalRepository savingsGoalRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private SavingsGoalServiceImpl savingsGoalService;

    private User user;
    private User otherUser;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        otherUser = User.builder().id(2L).username("other@example.com").build();

        goal = SavingsGoal.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now())
                .user(user)
                .build();
    }

    @Test
    void createGoal_Success() {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setTargetDate(LocalDate.now().plusYears(1));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goal);
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), eq(TransactionType.INCOME), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), eq(TransactionType.EXPENSE), any()))
                .thenReturn(BigDecimal.ZERO);

        GoalResponse response = savingsGoalService.createGoal("test@example.com", request);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
    }

    @Test
    void createGoal_PastTargetDate_ThrowsBadRequest() {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Old Goal");
        request.setTargetAmount(new BigDecimal("1000.00"));
        request.setTargetDate(LocalDate.now().minusDays(1));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> savingsGoalService.createGoal("test@example.com", request));
    }

    @Test
    void getAllGoals_ReturnsGoals() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findByUser(user)).thenReturn(List.of(goal));
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000));

        GoalListResponse response = savingsGoalService.getAllGoals("test@example.com");

        assertNotNull(response);
        assertEquals(1, response.getGoals().size());
    }

    @Test
    void getGoal_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        GoalResponse response = savingsGoalService.getGoal("test@example.com", 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getGoal_OtherUserGoal_ThrowsForbidden() {
        goal.setUser(otherUser);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ForbiddenException.class,
                () -> savingsGoalService.getGoal("test@example.com", 1L));
    }

    @Test
    void getGoal_NotFound_ThrowsException() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> savingsGoalService.getGoal("test@example.com", 99L));
    }

    @Test
    void updateGoal_Success() {
        UpdateGoalRequest request = new UpdateGoalRequest();
        request.setTargetAmount(new BigDecimal("6000.00"));
        request.setTargetDate(LocalDate.now().plusYears(2));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goal);
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        GoalResponse response = savingsGoalService.updateGoal("test@example.com", 1L, request);

        assertNotNull(response);
        verify(savingsGoalRepository).save(any(SavingsGoal.class));
    }

    @Test
    void updateGoal_PastTargetDate_ThrowsBadRequest() {
        UpdateGoalRequest request = new UpdateGoalRequest();
        request.setTargetDate(LocalDate.now().minusDays(1));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(BadRequestException.class,
                () -> savingsGoalService.updateGoal("test@example.com", 1L, request));
    }

    @Test
    void deleteGoal_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertDoesNotThrow(() -> savingsGoalService.deleteGoal("test@example.com", 1L));
        verify(savingsGoalRepository).delete(goal);
    }

    @Test
    void deleteGoal_OtherUserGoal_ThrowsForbidden() {
        goal.setUser(otherUser);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ForbiddenException.class,
                () -> savingsGoalService.deleteGoal("test@example.com", 1L));
    }
}
