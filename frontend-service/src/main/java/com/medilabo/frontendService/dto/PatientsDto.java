package com.medilabo.frontendService.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PatientsDto {

    private List<Patient> data;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    @Getter
    @Setter
    public static class Patient extends PatientDto {
        private UUID id;
    }
}
