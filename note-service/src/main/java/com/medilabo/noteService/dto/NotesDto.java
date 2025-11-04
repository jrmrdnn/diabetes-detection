package com.medilabo.noteService.dto;

import com.medilabo.noteService.model.Note;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for paginated notes.
 */
@Getter
@Setter
public class NotesDto {

  private List<Note> data;
  private long totalElements;
  private int totalPages;
  private int currentPage;
  private int pageSize;
}
