package com.medilabo.noteService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class NoteServiceApplicationTests {

  @Test
  void contextLoads() {
    assertDoesNotThrow(() -> NoteServiceApplication.main(new String[]{}));
  }
}
