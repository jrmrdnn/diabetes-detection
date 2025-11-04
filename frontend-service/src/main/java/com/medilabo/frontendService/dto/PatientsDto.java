package com.medilabo.frontendService.dto;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for paginated patients.
 */
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