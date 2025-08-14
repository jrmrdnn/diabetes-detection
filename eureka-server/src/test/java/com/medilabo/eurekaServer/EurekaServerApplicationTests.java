package com.medilabo.eurekaServer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class EurekaServerApplicationTests {

  @Test
  void contextLoads() {
    String[] args = { "--spring.profiles.active=test" };
    assertDoesNotThrow(() -> EurekaServerApplication.main(args));
  }
}
