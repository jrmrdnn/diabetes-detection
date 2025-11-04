package com.medilabo.noteService.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for Note.
 */
@Getter
@Setter
public class NoteDto {

  private String patient;
  private String note;
}
