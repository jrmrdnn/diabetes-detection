package com.medilabo.noteService.controller;

import com.medilabo.noteService.dto.NoteDto;
import com.medilabo.noteService.dto.NotesDto;
import com.medilabo.noteService.model.Note;
import com.medilabo.noteService.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NoteControllerTest {

    @InjectMocks
    private NoteController noteController;

    @Mock
    private NoteService noteService;

    private Note note;
    private NoteDto noteDto;
    private NotesDto notesDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        note = new Note();
        note.setId("id1");
        note.setPatient("patient1");
        note.setNote("Some note content");

        noteDto = new NoteDto();
        noteDto.setPatient("patient1");
        noteDto.setNote("Some note content");

        notesDto = new NotesDto();
        notesDto.setData(List.of(note));
        notesDto.setCurrentPage(0);
        notesDto.setTotalPages(1);
        notesDto.setTotalElements(1);
        notesDto.setPageSize(1);
    }

    @Test
    void getNotesByPatient_shouldReturnNotesDto() {
        when(
                noteService.getNotesByPatient(eq("patient1"), any(PageRequest.class))
        ).thenReturn(notesDto);

        NotesDto result = noteController.getNotesByPatient("patient1", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("id1", result.getData().get(0).getId());
        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getPageSize());
    }

    @Test
    void getAllNotesByPatient_shouldReturnListOfNotes() {
        List<Note> notes = Collections.singletonList(note);
        when(noteService.getAllNotesByPatient("patient1")).thenReturn(notes);

        List<Note> result = noteController.getAllNotesByPatient("patient1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("id1", result.getFirst().getId());
    }

    @Test
    void addNote_shouldCallService() {
        doNothing().when(noteService).addNote(any(NoteDto.class));

        noteController.addNote(noteDto);

        verify(noteService, times(1)).addNote(any(NoteDto.class));
    }

    @Test
    void updateNote_shouldCallService() {
        doNothing().when(noteService).updateNote(eq("id1"), any(NoteDto.class));

        noteController.updateNote("id1", noteDto);

        verify(noteService, times(1)).updateNote(eq("id1"), any(NoteDto.class));
    }

    @Test
    void deleteNote_shouldCallService() {
        doNothing().when(noteService).deleteNote("id1");

        noteController.deleteNote("id1");

        verify(noteService, times(1)).deleteNote("id1");
    }
}
