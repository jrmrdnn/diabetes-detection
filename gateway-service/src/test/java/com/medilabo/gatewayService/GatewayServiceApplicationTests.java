package com.medilabo.gatewayService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class GatewayServiceApplicationTests {

  @Test
  void contextLoads() {
    String[] args = { "--spring.profiles.active=test" };
    assertDoesNotThrow(() -> GatewayServiceApplication.main(args));
  }
}
