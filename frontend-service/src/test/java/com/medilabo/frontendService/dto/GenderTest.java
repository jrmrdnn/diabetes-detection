package com.medilabo.frontendService.dto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GenderTest {

  @Test
  void testValues_countAndOrder() {
    Gender[] values = Gender.values();
    assertEquals(2, values.length);
    assertArrayEquals(new Gender[] { Gender.F, Gender.M }, values);
  }

  @Test
  void testValueOf_validNames() {
    assertEquals(Gender.F, Gender.valueOf("F"));
    assertEquals(Gender.M, Gender.valueOf("M"));
  }

  @Test
  void testNames_matchExpected() {
    assertEquals("F", Gender.F.name());
    assertEquals("M", Gender.M.name());
  }

  @Test
  void testValueOf_invalidName_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> Gender.valueOf("X"));
    assertThrows(IllegalArgumentException.class, () -> Gender.valueOf(""));
    assertThrows(IllegalArgumentException.class, () -> Gender.valueOf("f"));
  }
}
