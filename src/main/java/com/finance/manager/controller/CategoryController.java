package com.finance.manager.controller;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryListResponse;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for category management operations.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Returns all categories accessible to the authenticated user.
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        CategoryListResponse response = categoryService.getAllCategories(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new custom category.
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes a custom category by name.
     * DELETE /api/categories/{name}
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String name) {
        categoryService.deleteCategory(userDetails.getUsername(), name);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully"));
    }
}
