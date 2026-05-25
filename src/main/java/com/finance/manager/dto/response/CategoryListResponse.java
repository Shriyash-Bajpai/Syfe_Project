package com.finance.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Response DTO wrapping a list of categories.
 */
@Data
@AllArgsConstructor
public class CategoryListResponse {
    private List<CategoryResponse> categories;
}
