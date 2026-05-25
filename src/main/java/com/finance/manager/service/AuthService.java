package com.finance.manager.service;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.dto.response.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Service interface for user authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     *
     * @param request registration details
     * @return response containing success message and new user ID
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Authenticates a user and creates a session.
     *
     * @param request  login credentials
     * @param httpRequest  servlet request to create session on
     * @param httpResponse servlet response to set cookie on
     * @return success message response
     */
    MessageResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * Invalidates the current user session.
     *
     * @param httpRequest servlet request containing the session to invalidate
     * @return success message response
     */
    MessageResponse logout(HttpServletRequest httpRequest);
}
