package com.medilabo.assessmentService.feign;

import com.medilabo.assessmentService.config.FeignConfig;
import com.medilabo.assessmentService.dto.NoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Feign client for interacting with the Note Service.
 * This client provides methods to retrieve notes associated with a patient.
 */
@FeignClient(name = "note-service", path = "/api/notes", url = "http://localhost:8084", configuration = FeignConfig.class)
public interface NoteFeignClient {
    /**
     * Retrieves all notes associated with a specific patient.
     *
     * @param id the UUID of the patient
     * @return a list of NoteDto objects representing the notes for the patient
     */
    @GetMapping("/all/patient/{id}")
    List<NoteDto> getAllNotesByPatient(@PathVariable UUID id);
}
