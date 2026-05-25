package com.finance.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Response DTO wrapping a list of savings goals.
 */
@Data
@AllArgsConstructor
public class GoalListResponse {
    private List<GoalResponse> goals;
}
