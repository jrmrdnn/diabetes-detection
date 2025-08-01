package com.medilabo.frontendService.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotesDtoTest {

  @Test
  void testDataGetterAndSetter() {
    NotesDto dto = new NotesDto();
    assertNull(dto.getData());

    NotesDto.Note note1 = new NotesDto.Note();
    LocalDateTime now = LocalDateTime.now();
    note1.setId("1");
    note1.setCreatedAt(now);
    note1.setUpdatedAt(now.plusDays(1));

    NotesDto.Note note2 = new NotesDto.Note();
    note2.setId("2");
    note2.setCreatedAt(now.minusDays(1));
    note2.setUpdatedAt(now);

    List<NotesDto.Note> dataList = Arrays.asList(note1, note2);
    dto.setData(dataList);

    assertNotNull(dto.getData());
    assertIterableEquals(dataList, dto.getData());
  }

  @Test
  void testNoteFieldsDefaultAndSettersGetters() {
    NotesDto.Note note = new NotesDto.Note();
    assertNull(note.getId());
    assertNull(note.getCreatedAt());
    assertNull(note.getUpdatedAt());

    String id = "noteId";
    LocalDateTime createdAt = LocalDateTime.of(2021, 5, 15, 10, 30);
    LocalDateTime updatedAt = LocalDateTime.of(2021, 5, 16, 11, 45);

    note.setId(id);
    note.setCreatedAt(createdAt);
    note.setUpdatedAt(updatedAt);

    assertEquals(id, note.getId());
    assertEquals(createdAt, note.getCreatedAt());
    assertEquals(updatedAt, note.getUpdatedAt());
  }
}
