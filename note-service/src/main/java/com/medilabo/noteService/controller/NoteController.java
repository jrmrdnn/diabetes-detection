package com.medilabo.noteService.controller;

import com.medilabo.noteService.dto.NoteDto;
import com.medilabo.noteService.dto.NotesDto;
import com.medilabo.noteService.model.Note;
import com.medilabo.noteService.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling note-related requests.
 * Provides endpoints to manage notes for patients.
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Retrieves paginated notes for a specific patient.
     *
     * @param patient the patient identifier
     * @param page    the page number to retrieve
     * @param size    the number of notes per page
     * @return a paginated list of notes for the specified patient
     */
    @GetMapping("/patient/{patient}")
    public NotesDto getNotesByPatient(
            @PathVariable String patient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "createdAt"));
        return noteService.getNotesByPatient(patient, pageRequest);
    }

    /**
     * Retrieves all notes for a specific patient.
     *
     * @param patient the patient identifier
     * @return a list of all notes for the specified patient
     */
    @GetMapping("/all/patient/{patient}")
    public List<Note> getAllNotesByPatient(@PathVariable String patient) {
        return noteService.getAllNotesByPatient(patient);
    }

    /**
     * Retrieves a specific note by its ID.
     */
    @PostMapping
    public void addNote(@RequestBody NoteDto noteDto) {
        noteService.addNote(noteDto);
    }

    /**
     * Updates an existing note.
     *
     * @param id      the note identifierw
     * @param noteDto the updated note data
     */
    @PutMapping("/{id}")
    public void updateNote(@PathVariable String id, @RequestBody NoteDto noteDto) {
        noteService.updateNote(id, noteDto);
    }

    /**
     * Deletes a note by its ID.
     *
     * @param id the note identifier
     */
    @DeleteMapping("/{id}")
    public void deleteNote(@PathVariable String id) {
        noteService.deleteNote(id);
    }
}
