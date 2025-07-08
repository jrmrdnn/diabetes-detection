package com.medilabo.authService.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import com.medilabo.authService.model.User;
import com.medilabo.authService.repository.UserRepository;
import com.medilabo.authService.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Service class for handling authentication operations.
 * It provides methods to authenticate users and manage password encoding.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;

  @Value("${argon2.iterations}")
  private int iterations;

  @Value("${argon2.memory}")
  private int memory;

  @Value("${argon2.parallelism}")
  private int parallelism;

  @Value("${argon2.saltLength}")
  private int saltLength;

  @Value("${argon2.hashLength}")
  private int hashLength;

  /**
   * Authenticates a user by username and password.
   * If the user is found and the password matches, it generates a JWT token.
   *
   * <p>
   *  This method retrieves the user by username, checks the password using Argon2 encoding,
   *  and generates a JWT token using the JwtUtil class.
   *  The token is set in the HTTP response header.
   * </p>
   * @param username the username of the user
   * @param password the password of the user
   * @param httpRequest the HTTP request to extract the user's IP address
   * @param httpResponse the HTTP response to set the token in
   * @return a JWT token as a string
   * @throws IllegalArgumentException if the user is not found or password does not match
   * @see UserRepository
   * @see JwtUtil
   * @see HttpServletRequest
   * @see HttpServletResponse
   */
  public String authenticate(
    String username,
    String password,
    HttpServletRequest httpRequest,
    HttpServletResponse httpResponse
  ) {
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() -> new IllegalArgumentException("User not found")
      );

    if (!passwordEncoder().matches(password, user.getPassword())) {
      throw new IllegalArgumentException("User not found");
    }

    return jwtUtil.generateToken(
      user.getId().toString(),
      user.getUsername(),
      httpRequest
    );
  }

  /**
   * Returns a new Argon2PasswordEncoder instance configured with the properties defined in application properties.
   *
   * @return a configured Argon2PasswordEncoder instance
   * @see Argon2PasswordEncoder
   */
  private Argon2PasswordEncoder passwordEncoder() {
    return new Argon2PasswordEncoder(
      saltLength,
      hashLength,
      iterations,
      memory,
      parallelism
    );
  }
}
