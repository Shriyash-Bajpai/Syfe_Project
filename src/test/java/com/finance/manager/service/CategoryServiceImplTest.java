package com.finance.manager.service;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryListResponse;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.enums.TransactionType;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.DuplicateResourceException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User user;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();

        defaultCategory = Category.builder()
                .id(1L).name("Salary").type(TransactionType.INCOME).isCustom(false).build();

        customCategory = Category.builder()
                .id(2L).name("Freelance").type(TransactionType.INCOME).isCustom(true).user(user).build();
    }

    @Test
    void getAllCategories_ReturnsDefaultAndCustom() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findAllAccessibleByUser(user))
                .thenReturn(List.of(defaultCategory, customCategory));

        CategoryListResponse response = categoryService.getAllCategories("test@example.com");

        assertNotNull(response);
        assertEquals(2, response.getCategories().size());
    }

    @Test
    void createCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Freelance");
        request.setType(TransactionType.INCOME);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.existsByNameAndUser("Freelance", user)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryResponse response = categoryService.createCategory("test@example.com", request);

        assertNotNull(response);
        assertEquals("Freelance", response.getName());
        assertTrue(response.isCustom());
    }

    @Test
    void createCategory_ConflictsWithDefault_ThrowsDuplicate() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Salary");
        request.setType(TransactionType.INCOME);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("Salary")).thenReturn(Optional.of(defaultCategory));

        assertThrows(DuplicateResourceException.class,
                () -> categoryService.createCategory("test@example.com", request));
    }

    @Test
    void createCategory_DuplicateCustom_ThrowsDuplicate() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Freelance");
        request.setType(TransactionType.INCOME);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.existsByNameAndUser("Freelance", user)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> categoryService.createCategory("test@example.com", request));
    }

    @Test
    void deleteCategory_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory("test@example.com", "Freelance"));
        verify(categoryRepository).delete(customCategory);
    }

    @Test
    void deleteCategory_DefaultCategory_ThrowsForbidden() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("Salary")).thenReturn(Optional.of(defaultCategory));

        assertThrows(ForbiddenException.class,
                () -> categoryService.deleteCategory("test@example.com", "Salary"));
    }

    @Test
    void deleteCategory_InUse_ThrowsBadRequest() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> categoryService.deleteCategory("test@example.com", "Freelance"));
    }

    @Test
    void deleteCategory_NotFound_ThrowsResourceNotFound() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndIsCustomFalse("Ghost")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Ghost", user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory("test@example.com", "Ghost"));
    }
}
