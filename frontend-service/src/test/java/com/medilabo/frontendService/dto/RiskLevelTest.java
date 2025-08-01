package com.medilabo.frontendService.dto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RiskLevelTest {

  @Test
  void testValues_countAndOrder() {
    RiskLevel[] values = RiskLevel.values();
    assertEquals(4, values.length, "There should be four risk levels");
    assertArrayEquals(
      new RiskLevel[] {
        RiskLevel.NONE,
        RiskLevel.BORDERLINE,
        RiskLevel.IN_DANGER,
        RiskLevel.EARLY_ONSET,
      },
      values,
      "Enum values should be in the defined order"
    );
  }

  @Test
  void testDescriptions_matchExpected() {
    assertEquals("None", RiskLevel.NONE.getDescription());
    assertEquals("Borderline", RiskLevel.BORDERLINE.getDescription());
    assertEquals("In Danger", RiskLevel.IN_DANGER.getDescription());
    assertEquals("Early onset", RiskLevel.EARLY_ONSET.getDescription());
  }

  @Test
  void testValueOf_validNames() {
    assertEquals(RiskLevel.NONE, RiskLevel.valueOf("NONE"));
    assertEquals(RiskLevel.BORDERLINE, RiskLevel.valueOf("BORDERLINE"));
    assertEquals(RiskLevel.IN_DANGER, RiskLevel.valueOf("IN_DANGER"));
    assertEquals(RiskLevel.EARLY_ONSET, RiskLevel.valueOf("EARLY_ONSET"));
  }

  @Test
  void testDescriptions_notNullOrEmpty() {
    for (RiskLevel level : RiskLevel.values()) {
      String desc = level.getDescription();
      assertNotNull(desc, "Description should not be null for " + level.name());
      assertFalse(
        desc.isBlank(),
        "Description should not be blank for " + level.name()
      );
    }
  }
}
