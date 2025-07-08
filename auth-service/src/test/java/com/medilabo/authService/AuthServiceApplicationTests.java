package com.medilabo.authService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class AuthServiceApplicationTests {

  @Test
  void contextLoads() {
    String[] args = { "--spring.profiles.active=test" };
    assertDoesNotThrow(() -> AuthServiceApplication.main(args));
  }
}
