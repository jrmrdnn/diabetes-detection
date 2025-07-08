package com.medilabo.authService.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medilabo.authService.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Controller for handling authentication requests.
 * Provides endpoints for user login and retrieving the public key.
 */
@Log4j2
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint for user login.
     * Accepts a username and password, authenticates the user, and sets a session cookie.
     *
     * @param request      the login request containing username and password
     * @param httpRequest  the HTTP request
     * @param httpResponse the HTTP response
     * @return a response indicating success or failure of the login attempt
     */
    @PostMapping("/auth")
    public String login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            return authService.authenticate(
                    request.username(),
                    request.password(),
                    httpRequest,
                    httpResponse
            );
        } catch (IllegalArgumentException e) {
            return "User not found";
        } catch (Exception e) {
            log.error(
                    "Error during login for user {}: {}",
                    request.username(),
                    e.getMessage()
            );
            return "Error during login";
        }
    }

    public record LoginRequest(String username, String password) {
    }
}