package com.medilabo.frontendService.feign;

import com.medilabo.frontendService.config.FeignConfig;
import com.medilabo.frontendService.dto.NoteDto;
import com.medilabo.frontendService.dto.NotesDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "note-service", path = "/api/notes", url = "${NOTE_SERVICE_URL:http://localhost:8084}", configuration = FeignConfig.class)
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
