package com.medilabo.noteService.service;

import com.medilabo.noteService.dto.NoteDto;
import com.medilabo.noteService.dto.NotesDto;
import com.medilabo.noteService.model.Note;
import com.medilabo.noteService.repository.NoteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service for managing notes.
 * Provides methods to retrieve, add, update, and delete notes for patients.
 */
@Service
@RequiredArgsConstructor
public class NoteService {

  private final NoteRepository noteRepository;

  /**
   * Retrieves a note by its ID.
   *
   * @param id the note identifier
   * @return the note with the specified ID
   */
  public Note getNoteById(String id) {
    return noteRepository
      .findById(id)
      .orElseThrow(() -> new RuntimeException("Note not found"));
  }

  /**
   * Retrieves paginated notes for a specific patient.
   *
   * @param patient the patient identifier
   * @param pageable the pagination information
   * @return a paginated list of notes for the specified patient
   */
  public NotesDto getNotesByPatient(String patient, Pageable pageable) {
    Page<Note> notePage = noteRepository.findByPatient(patient, pageable);
    return getNotesDto(notePage);
  }

  /**
   * Retrieves all notes for a specific patient.
   *
   * @param patient the patient identifier
   * @return a list of all notes for the specified patient
   */
  public List<Note> getAllNotesByPatient(String patient) {
    return noteRepository.findByPatient(patient);
  }

  /**
   * Adds a new note.
   *
   * @param noteDto the note data to be added
   */
  public void addNote(NoteDto noteDto) {
    Note newNote = new Note();
    newNote.setPatient(noteDto.getPatient());
    newNote.setNote(noteDto.getNote());
    noteRepository.save(newNote);
  }

  /**
   * Updates an existing note.
   *
   * @param id the note identifier
   * @param noteDto the updated note data
   */
  public void updateNote(String id, NoteDto noteDto) {
    Note existingNote = getNoteById(id);
    existingNote.setNote(noteDto.getNote());
    noteRepository.save(existingNote);
  }

  /**
   * Deletes a note by its ID.
   *
   * @param id the note identifier
   */
  public void deleteNote(String id) {
    Note existingNote = getNoteById(id);
    noteRepository.delete(existingNote);
  }

  /**
   * Converts a Page of Note entities to a NotesDto.
   *
   * @param notePage the page of notes
   * @return a NotesDto containing the paginated notes
   */
  private NotesDto getNotesDto(Page<Note> notePage) {
    NotesDto notesDto = new NotesDto();
    notesDto.setTotalElements(notePage.getTotalElements());
    notesDto.setTotalPages(notePage.getTotalPages());
    notesDto.setCurrentPage(notePage.getNumber());
    notesDto.setPageSize(notePage.getSize());
    notesDto.setData(notePage.getContent());
    return notesDto;
  }
}
