package com.medilabo.authService.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medilabo.authService.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthControllerTest {

  private AuthService authService;
  private AuthController authController;
  private HttpServletRequest httpServletRequest;
  private HttpServletResponse httpServletResponse;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    authController = new AuthController(authService);
    httpServletRequest = mock(HttpServletRequest.class);
    httpServletResponse = mock(HttpServletResponse.class);
  }

  @Test
  void login_shouldReturnSuccess_whenAuthenticationSucceeds() {
    AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(
      "user",
      "pass"
    );
    when(
      authService.authenticate(
        eq("user"),
        eq("pass"),
        any(HttpServletRequest.class),
        any(HttpServletResponse.class)
      )
    ).thenReturn("Success");

    String result = authController.login(
      loginRequest,
      httpServletRequest,
      httpServletResponse
    );

    assertEquals("Success", result);
    verify(authService).authenticate(
      "user",
      "pass",
      httpServletRequest,
      httpServletResponse
    );
  }

  @Test
  void login_shouldReturnUserNotFound_whenIllegalArgumentExceptionThrown() {
    AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(
      "user",
      "wrongpass"
    );
    when(
      authService.authenticate(
        anyString(),
        anyString(),
        any(HttpServletRequest.class),
        any(HttpServletResponse.class)
      )
    ).thenThrow(new IllegalArgumentException("User not found"));

    String result = authController.login(
      loginRequest,
      httpServletRequest,
      httpServletResponse
    );

    assertEquals("User not found", result);
  }

  @Test
  void login_shouldReturnErrorDuringLogin_whenOtherExceptionThrown() {
    AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(
      "user",
      "pass"
    );
    when(
      authService.authenticate(
        anyString(),
        anyString(),
        any(HttpServletRequest.class),
        any(HttpServletResponse.class)
      )
    ).thenThrow(new RuntimeException("Unexpected error"));

    String result = authController.login(
      loginRequest,
      httpServletRequest,
      httpServletResponse
    );

    assertEquals("Error during login", result);
  }
}
