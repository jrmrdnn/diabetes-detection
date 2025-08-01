package com.medilabo.frontendService.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class AssessmentDtoTest {

  @Test
  void defaultConstructor_initializesWithNullRiskLevelAndZeroCount() {
    AssessmentDto dto = new AssessmentDto();
    assertNull(dto.getRiskLevel());
    assertEquals(0, dto.getTriggerTermsCount());
  }

  @Test
  void settersAndGetters_workAsExpected() {
    AssessmentDto dto = new AssessmentDto();
    RiskLevel someLevel = RiskLevel.values().length > 0
      ? RiskLevel.values()[0]
      : null;
    dto.setRiskLevel(someLevel);
    dto.setTriggerTermsCount(5);

    assertEquals(someLevel, dto.getRiskLevel());
    assertEquals(5, dto.getTriggerTermsCount());
  }

  @Test
  void acceptsNullRiskLevelAndNegativeTriggerCount() {
    AssessmentDto dto = new AssessmentDto();
    dto.setRiskLevel(null);
    dto.setTriggerTermsCount(-10);

    assertNull(dto.getRiskLevel());
    assertEquals(-10, dto.getTriggerTermsCount());
  }
}
