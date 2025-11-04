package com.medilabo.frontendService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.medilabo.frontendService.config.FeignConfig;
import com.medilabo.frontendService.dto.NoteDto;
import com.medilabo.frontendService.dto.NotesDto;

/**
 * Feign client for communicating with the Note Service.
 */
@FeignClient(name = "note-service", path = "/api/notes", configuration = FeignConfig.class)
public interface NoteFeignClient {
    @GetMapping("/patient/{patient}")
    NotesDto getNotesByPatient(
            @PathVariable String patient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size);

    @PostMapping
    void addNote(@RequestBody NoteDto noteDto);

    @PutMapping("/{id}")
    void updateNote(@PathVariable String id, @RequestBody NoteDto noteDto);

    @DeleteMapping("/{id}")
    void deleteNote(@PathVariable String id);
}
