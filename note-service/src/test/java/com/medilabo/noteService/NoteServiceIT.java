package com.medilabo.noteService;

import com.medilabo.noteService.dto.NoteDto;
import com.medilabo.noteService.model.Note;
import com.medilabo.noteService.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteServiceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteRepository noteRepository;

    @Test
    void addNote_shouldCreateNote_whenValidData() throws Exception {
        String patientId = UUID.randomUUID().toString();
        NoteDto noteDto = new NoteDto();
        noteDto.setPatient(patientId);
        noteDto.setNote("Test note content");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patient\":\"" + patientId + "\",\"note\":\"Test note content\"}"))
                .andExpect(status().isOk());

        List<Note> notes = noteRepository.findByPatient(patientId);
        assertFalse(notes.isEmpty());
        assertEquals("Test note content", notes.get(0).getNote());
    }

    @Test
    void getNotesByPatient_shouldReturnPaginatedNotes_whenNotesExist() throws Exception {
        String patientId = UUID.randomUUID().toString();
        Note note = new Note();
        note.setPatient(patientId);
        note.setNote("Note 1");
        note.setCreatedAt(LocalDateTime.now());
        noteRepository.save(note);

        mockMvc.perform(get("/api/notes/patient/" + patientId)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].note").value("Note 1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllNotesByPatient_shouldReturnAllNotes_whenNotesExist() throws Exception {
        String patientId = UUID.randomUUID().toString();
        Note note1 = new Note();
        note1.setPatient(patientId);
        note1.setNote("Note 1");
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setPatient(patientId);
        note2.setNote("Note 2");
        noteRepository.save(note2);

        mockMvc.perform(get("/api/notes/all/patient/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].note").value("Note 1"))
                .andExpect(jsonPath("$[1].note").value("Note 2"));
    }

    @Test
    void updateNote_shouldModifyNote_whenNoteExists() throws Exception {
        Note note = new Note();
        note.setPatient(UUID.randomUUID().toString());
        note.setNote("Original note");
        note = noteRepository.save(note);

        NoteDto updatedNoteDto = new NoteDto();
        updatedNoteDto.setPatient(note.getPatient());
        updatedNoteDto.setNote("Updated note");

        mockMvc.perform(put("/api/notes/" + note.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patient\":\"" + note.getPatient() + "\",\"note\":\"Updated note\"}"))
                .andExpect(status().isOk());

        Note updatedNote = noteRepository.findById(note.getId()).orElseThrow();
        assertEquals("Updated note", updatedNote.getNote());
    }

    @Test
    void deleteNote_shouldRemoveNote_whenNoteExists() throws Exception {
        Note note = new Note();
        note.setPatient(UUID.randomUUID().toString());
        note.setNote("Note to delete");
        note = noteRepository.save(note);

        mockMvc.perform(delete("/api/notes/" + note.getId()))
                .andExpect(status().isOk());

        assertFalse(noteRepository.findById(note.getId()).isPresent());
    }

    @Test
    void getNotesByPatient_shouldReturnEmptyList_whenNoNotesExist() throws Exception {
        String patientId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/notes/patient/" + patientId)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}