package com.finance.manager.controller;

import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.GoalListResponse;
import com.finance.manager.dto.response.GoalResponse;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for savings goal management operations.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    /**
     * Creates a new savings goal.
     * POST /api/goals
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GoalRequest request) {
        GoalResponse response = savingsGoalService.createGoal(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns all savings goals for the authenticated user.
     * GET /api/goals
     */
    @GetMapping
    public ResponseEntity<GoalListResponse> getAllGoals(
            @AuthenticationPrincipal UserDetails userDetails) {
        GoalListResponse response = savingsGoalService.getAllGoals(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a specific savings goal by ID.
     * GET /api/goals/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        GoalResponse response = savingsGoalService.getGoal(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates target amount and/or target date of an existing goal.
     * PUT /api/goals/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest request) {
        GoalResponse response = savingsGoalService.updateGoal(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a savings goal by ID.
     * DELETE /api/goals/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        savingsGoalService.deleteGoal(userDetails.getUsername(), id);
        return ResponseEntity.ok(new MessageResponse("Goal deleted successfully"));
    }
}
