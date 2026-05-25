package com.finance.manager.service.impl;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryListResponse;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.DuplicateResourceException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService for managing transaction categories.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CategoryListResponse getAllCategories(String username) {
        User user = getUser(username);

        List<CategoryResponse> responses = categoryRepository.findAllAccessibleByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new CategoryListResponse(responses);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(String username, CategoryRequest request) {
        User user = getUser(username);

        // Check conflict: same name as a default category
        if (categoryRepository.findByNameAndIsCustomFalse(request.getName()).isPresent()) {
            throw new DuplicateResourceException("A default category with name '" + request.getName() + "' already exists");
        }

        // Check conflict: same name among user's custom categories
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new DuplicateResourceException("Custom category '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .isCustom(true)
                .user(user)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(String username, String categoryName) {
        User user = getUser(username);

        // Check if it's a default category (forbidden to delete)
        if (categoryRepository.findByNameAndIsCustomFalse(categoryName).isPresent()) {
            throw new ForbiddenException("Default categories cannot be deleted");
        }

        Category category = categoryRepository.findByNameAndUser(categoryName, user)
                .orElseThrow(() -> new ResourceNotFoundException("Custom category not found: " + categoryName));

        // Check if the category is referenced by any transaction
        if (transactionRepository.existsByCategory(category)) {
            throw new BadRequestException("Cannot delete category '" + categoryName + "' because it is referenced by existing transactions");
        }

        categoryRepository.delete(category);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Maps a Category entity to its response DTO.
     */
    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .name(category.getName())
                .type(category.getType())
                .isCustom(category.isCustom())
                .build();
    }
}
