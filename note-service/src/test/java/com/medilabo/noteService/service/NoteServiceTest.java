package com.medilabo.noteService.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medilabo.noteService.dto.NoteDto;
import com.medilabo.noteService.dto.NotesDto;
import com.medilabo.noteService.model.Note;
import com.medilabo.noteService.repository.NoteRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class NoteServiceTest {

  @Mock
  private NoteRepository noteRepository;

  @InjectMocks
  private NoteService noteService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getNoteById_shouldReturnNote_whenFound() {
    Note note = new Note();
    note.setId("1");
    when(noteRepository.findById("1")).thenReturn(Optional.of(note));

    Note result = noteService.getNoteById("1");

    assertThat(result).isEqualTo(note);
  }

  @Test
  void getNoteById_shouldThrowException_whenNotFound() {
    when(noteRepository.findById("1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> noteService.getNoteById("1"))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Note not found");
  }

  @Test
  void getNotesByPatient_shouldReturnNotesDto() {
    Note note = new Note();
    note.setId("1");
    List<Note> notes = Collections.singletonList(note);
    Page<Note> notePage = new PageImpl<>(notes, PageRequest.of(0, 10), 1);

    when(
      noteRepository.findByPatient(eq("patient1"), any(Pageable.class))
    ).thenReturn(notePage);

    NotesDto result = noteService.getNotesByPatient(
      "patient1",
      PageRequest.of(0, 10)
    );

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getData()).containsExactly(note);
  }

  @Test
  void getAllNotesByPatient_shouldReturnList() {
    Note note = new Note();
    note.setId("1");
    List<Note> notes = Collections.singletonList(note);

    when(noteRepository.findByPatient("patient1")).thenReturn(notes);

    List<Note> result = noteService.getAllNotesByPatient("patient1");

    assertThat(result).containsExactly(note);
  }

  @Test
  void addNote_shouldSaveNote() {
    NoteDto noteDto = new NoteDto();
    noteDto.setPatient("patient1");
    noteDto.setNote("test note");

    Note savedNote = new Note();
    savedNote.setPatient("patient1");
    savedNote.setNote("test note");

    when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

    noteService.addNote(noteDto);

    ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
    verify(noteRepository).save(captor.capture());
    Note captured = captor.getValue();
    assertThat(captured.getPatient()).isEqualTo("patient1");
    assertThat(captured.getNote()).isEqualTo("test note");
  }

  @Test
  void updateNote_shouldUpdateAndSaveNote() {
    Note existingNote = new Note();
    existingNote.setId("1");
    existingNote.setNote("old note");

    NoteDto noteDto = new NoteDto();
    noteDto.setNote("new note");

    when(noteRepository.findById("1")).thenReturn(Optional.of(existingNote));
    when(noteRepository.save(any(Note.class))).thenReturn(existingNote);

    noteService.updateNote("1", noteDto);

    assertThat(existingNote.getNote()).isEqualTo("new note");
    verify(noteRepository).save(existingNote);
  }

  @Test
  void deleteNote_shouldDeleteNote() {
    Note existingNote = new Note();
    existingNote.setId("1");

    when(noteRepository.findById("1")).thenReturn(Optional.of(existingNote));

    noteService.deleteNote("1");

    verify(noteRepository).delete(existingNote);
  }
}
