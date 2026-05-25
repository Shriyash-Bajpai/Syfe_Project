package com.finance.manager.service;

import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.GoalListResponse;
import com.finance.manager.dto.response.GoalResponse;

/**
 * Service interface for savings goal management operations.
 */
public interface SavingsGoalService {

    /**
     * Creates a new savings goal for the authenticated user.
     */
    GoalResponse createGoal(String username, GoalRequest request);

    /**
     * Retrieves all savings goals for the authenticated user with progress data.
     */
    GoalListResponse getAllGoals(String username);

    /**
     * Retrieves a specific savings goal by ID with progress data.
     */
    GoalResponse getGoal(String username, Long id);

    /**
     * Updates the target amount and/or target date of an existing goal.
     */
    GoalResponse updateGoal(String username, Long id, UpdateGoalRequest request);

    /**
     * Deletes a savings goal.
     */
    void deleteGoal(String username, Long id);
}
