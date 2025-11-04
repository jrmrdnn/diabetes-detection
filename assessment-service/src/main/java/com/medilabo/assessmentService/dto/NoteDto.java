package com.medilabo.assessmentService.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for notes.
 */
@Getter
@Setter
public class NoteDto {
    private String patient;
    private String note;
}
