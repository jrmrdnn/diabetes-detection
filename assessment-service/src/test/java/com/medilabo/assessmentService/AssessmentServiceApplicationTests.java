package com.medilabo.assessmentService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class AssessmentServiceApplicationTests {

  @Test
  void contextLoads() {
    String[] args = { "--spring.profiles.active=test" };
    assertDoesNotThrow(() -> AssessmentServiceApplication.main(args));
  }
}
