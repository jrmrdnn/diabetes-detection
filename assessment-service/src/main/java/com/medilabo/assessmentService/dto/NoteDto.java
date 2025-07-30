package com.medilabo.assessmentService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class NoteDto {
    private String patient;
    private String note;
}
