package com.medilabo.frontendService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class FrontendServiceApplicationTests {

  @Test
  void contextLoads() {
    String[] args = { "--spring.profiles.active=test" };
    assertDoesNotThrow(() -> FrontendServiceApplication.main(args));
  }
}
