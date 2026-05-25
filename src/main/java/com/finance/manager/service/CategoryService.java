package com.finance.manager.service;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryListResponse;
import com.finance.manager.dto.response.CategoryResponse;

/**
 * Service interface for category management operations.
 */
public interface CategoryService {

    /**
     * Returns all categories accessible to the authenticated user (default + custom).
     */
    CategoryListResponse getAllCategories(String username);

    /**
     * Creates a new custom category for the authenticated user.
     */
    CategoryResponse createCategory(String username, CategoryRequest request);

    /**
     * Deletes a custom category by name for the authenticated user.
     * Cannot delete default categories or categories in use by transactions.
     */
    void deleteCategory(String username, String categoryName);
}
