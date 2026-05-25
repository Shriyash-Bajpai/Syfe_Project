package com.finance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryListResponse;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.enums.TransactionType;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.DuplicateResourceException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@WithMockUser(username = "test@example.com")
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CategoryService categoryService;

    @Test
    void getAllCategories_Returns200() throws Exception {
        CategoryResponse salary = CategoryResponse.builder()
                .name("Salary").type(TransactionType.INCOME).isCustom(false).build();
        CategoryResponse freelance = CategoryResponse.builder()
                .name("Freelance").type(TransactionType.INCOME).isCustom(true).build();

        when(categoryService.getAllCategories("test@example.com"))
                .thenReturn(new CategoryListResponse(List.of(salary, freelance)));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories.length()").value(2))
                .andExpect(jsonPath("$.categories[0].name").value("Salary"))
                .andExpect(jsonPath("$.categories[0].isCustom").value(false))
                .andExpect(jsonPath("$.categories[1].name").value("Freelance"))
                .andExpect(jsonPath("$.categories[1].isCustom").value(true));
    }

    @Test
    void createCategory_ValidRequest_Returns201() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Freelance");
        request.setType(TransactionType.INCOME);

        CategoryResponse response = CategoryResponse.builder()
                .name("Freelance").type(TransactionType.INCOME).isCustom(true).build();

        when(categoryService.createCategory(eq("test@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Freelance"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test
    void createCategory_MissingName_Returns400() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setType(TransactionType.INCOME);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_DuplicateName_Returns409() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Salary");
        request.setType(TransactionType.INCOME);

        when(categoryService.createCategory(any(), any()))
                .thenThrow(new DuplicateResourceException("Category already exists"));

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategory_Success_Returns200() throws Exception {
        doNothing().when(categoryService).deleteCategory("test@example.com", "Freelance");

        mockMvc.perform(delete("/api/categories/Freelance").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    void deleteCategory_DefaultCategory_Returns403() throws Exception {
        doThrow(new ForbiddenException("Default categories cannot be deleted"))
                .when(categoryService).deleteCategory("test@example.com", "Salary");

        mockMvc.perform(delete("/api/categories/Salary").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Default categories cannot be deleted"));
    }

    @Test
    void deleteCategory_InUse_Returns400() throws Exception {
        doThrow(new BadRequestException("Cannot delete category referenced by transactions"))
                .when(categoryService).deleteCategory("test@example.com", "Freelance");

        mockMvc.perform(delete("/api/categories/Freelance").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found: Ghost"))
                .when(categoryService).deleteCategory("test@example.com", "Ghost");

        mockMvc.perform(delete("/api/categories/Ghost").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
