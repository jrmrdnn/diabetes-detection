package com.medilabo.frontendService.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PatientsDto extends PaginatedDto {
    private List<Patient> data;

    @Getter
    @Setter
    public static class Patient extends PatientDto {
        private UUID id;
    }
}