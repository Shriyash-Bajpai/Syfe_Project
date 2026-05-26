package com.finance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.dto.response.RegisterResponse;
import com.finance.manager.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;

    @Test
    void register_ValidRequest_Returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user@example.com");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setPhoneNumber("+1234567890");

        when(authService.register(any())).thenReturn(new RegisterResponse("User registered successfully", 1L));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void register_InvalidEmail_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("not-an-email");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setPhoneNumber("+1234567890");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_MissingPassword_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user@example.com");
        request.setFullName("John Doe");
        request.setPhoneNumber("+1234567890");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidRequest_Returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("user@example.com");
        request.setPassword("password123");

        when(authService.login(any(), any(), any())).thenReturn(new MessageResponse("Login successful"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @WithMockUser
    void logout_AuthenticatedUser_Returns200() throws Exception {
        when(authService.logout(any())).thenReturn(new MessageResponse("Logout successful"));

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
