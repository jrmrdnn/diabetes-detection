package com.medilabo.noteService.repository;

import com.medilabo.noteService.model.Note;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Note entity.
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
  Page<Note> findByPatient(String patient, Pageable pageable);

  List<Note> findByPatient(String patient);
}
