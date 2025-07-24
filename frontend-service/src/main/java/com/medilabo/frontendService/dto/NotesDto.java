package com.medilabo.frontendService.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotesDto extends PaginatedDto {
  private List<Note> data;

    @Getter
    @Setter
    public static class Note extends NoteDto {
        private String id;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
