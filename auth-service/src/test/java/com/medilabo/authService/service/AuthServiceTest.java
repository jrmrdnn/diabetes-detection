package com.medilabo.authService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medilabo.authService.model.User;
import com.medilabo.authService.repository.UserRepository;
import com.medilabo.authService.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

class AuthServiceTest {

  private UserRepository userRepository;
  private JwtUtil jwtUtil;
  private AuthService authService;

  private final UUID userId = UUID.randomUUID();
  private final String username = "testuser";
  private final String password = "password123";

  private final int iterations = 2;
  private final int memory = 65536;
  private final int parallelism = 1;
  private final int saltLength = 16;
  private final int hashLength = 32;

  @BeforeEach
  void setUp() throws Exception {
    userRepository = mock(UserRepository.class);
    jwtUtil = mock(JwtUtil.class);

    authService = new AuthService(userRepository, jwtUtil);

    setField(authService, "iterations", iterations);
    setField(authService, "memory", memory);
    setField(authService, "parallelism", parallelism);
    setField(authService, "saltLength", saltLength);
    setField(authService, "hashLength", hashLength);
  }

  private void setField(Object target, String fieldName, Object value)
    throws Exception {
    var field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void authenticate_successful_returnsJwtToken() {
    Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(
      saltLength,
      hashLength,
      iterations,
      memory,
      parallelism
    );

    User user = new User();
    user.setId(userId);
    user.setUsername(username);
    user.setPassword(encoder.encode(password));

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    when(
      jwtUtil.generateToken(
        eq(userId.toString()),
        eq(username),
        any(HttpServletRequest.class)
      )
    ).thenReturn("jwt-token");

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String token = authService.authenticate(
      username,
      password,
      request,
      response
    );

    assertEquals("jwt-token", token);
    verify(userRepository).findByUsername(username);
    verify(jwtUtil).generateToken(
      eq(userId.toString()),
      eq(username),
      eq(request)
    );
  }

  @Test
  void authenticate_userNotFound_throwsException() {
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    assertThrows(IllegalArgumentException.class, () ->
      authService.authenticate(username, password, request, response)
    );
  }

  @Test
  void authenticate_passwordMismatch_throwsException() {
    String wrongPassword = "wrongpass";

    Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(
      saltLength,
      hashLength,
      iterations,
      memory,
      parallelism
    );
    String encodedPassword = encoder.encode(password);

    User user = new User();
    user.setId(userId);
    user.setUsername(username);
    user.setPassword(encodedPassword);

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    assertThrows(IllegalArgumentException.class, () ->
      authService.authenticate(username, wrongPassword, request, response)
    );
  }
}
