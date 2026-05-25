package com.finance.manager.service.impl;

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
import com.finance.manager.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of SavingsGoalService with progress calculation logic.
 */
@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GoalResponse createGoal(String username, GoalRequest request) {
        User user = getUser(username);

        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be a future date");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        return toResponse(savingsGoalRepository.save(goal), user);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalListResponse getAllGoals(String username) {
        User user = getUser(username);

        List<GoalResponse> goals = savingsGoalRepository.findByUser(user)
                .stream()
                .map(goal -> toResponse(goal, user))
                .collect(Collectors.toList());

        return new GoalListResponse(goals);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponse getGoal(String username, Long id) {
        User user = getUser(username);

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this goal");
        }

        return toResponse(goal, user);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(String username, Long id, UpdateGoalRequest request) {
        User user = getUser(username);

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this goal");
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be a future date");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        return toResponse(savingsGoalRepository.save(goal), user);
    }

    @Override
    @Transactional
    public void deleteGoal(String username, Long id) {
        User user = getUser(username);

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this goal");
        }

        savingsGoalRepository.delete(goal);
    }

    /**
     * Calculates savings progress: (Total Income - Total Expenses) since goal start date.
     * Deleted transactions are excluded automatically via the live transaction table.
     */
    private BigDecimal calculateProgress(SavingsGoal goal, User user) {
        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndDateAfter(
                user, TransactionType.INCOME, goal.getStartDate());
        BigDecimal totalExpenses = transactionRepository.sumByUserAndTypeAndDateAfter(
                user, TransactionType.EXPENSE, goal.getStartDate());

        return totalIncome.subtract(totalExpenses).max(BigDecimal.ZERO);
    }

    /**
     * Maps a SavingsGoal entity to its response DTO including calculated progress.
     */
    private GoalResponse toResponse(SavingsGoal goal, User user) {
        BigDecimal progress = calculateProgress(goal, user);
        BigDecimal remaining = goal.getTargetAmount().subtract(progress).max(BigDecimal.ZERO);

        double percentage = goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : progress.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .setScale(2, RoundingMode.HALF_UP)
                          .doubleValue();

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(progress)
                .progressPercentage(percentage)
                .remainingAmount(remaining)
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
