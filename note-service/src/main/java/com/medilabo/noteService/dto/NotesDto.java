package com.medilabo.noteService.dto;

import com.medilabo.noteService.model.Note;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NotesDto {

    private List<Note> data;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
