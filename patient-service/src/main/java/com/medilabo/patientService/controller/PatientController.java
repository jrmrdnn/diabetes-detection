package com.medilabo.patientService.controller;

import com.medilabo.patientService.dto.PatientDto;
import com.medilabo.patientService.dto.PatientsDto;
import com.medilabo.patientService.model.Patient;
import com.medilabo.patientService.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for managing patients.
 * Provides endpoints for CRUD operations on patients.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    /**
     * Retrieves all patients with pagination and sorting.
     *
     * @param page   the page number to retrieve
     * @param size   the number of patients per page
     * @param sort   the sort direction (asc or desc)
     * @param sortBy the field to sort by (default is lastName)
     * @return a PatientsDto containing the list of patients and pagination info
     */
    @GetMapping
    public PatientsDto getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestParam(defaultValue = "lastName") String sortBy
    ) {
        Direction direction = sort.equalsIgnoreCase("desc")
                ? Direction.DESC
                : Direction.ASC;

        String sortFinal =
                switch (sortBy) {
                    case "birthDate" -> "birthDate";
                    case "gender" -> "gender";
                    default -> "lastName";
                };

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortFinal)
        );

        return patientService.getAllPatients(pageRequest);
    }

    /**
     * Retrieves a patient by ID.
     *
     * @param id the ID of the patient to retrieve
     * @return the Patient object if found, or throws an exception if not found
     */
    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable UUID id) {
        return patientService
                .getPatientById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    /**
     * Searches for patients by name with pagination.
     *
     * @param q    the search query (name)
     * @param page the page number to retrieve
     * @param size the number of patients per page
     * @return a PatientsDto containing the list of matching patients and pagination info
     */
    @GetMapping("/search")
    public PatientsDto searchPatients(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return patientService.searchPatients(q, PageRequest.of(page, size));
    }

    /**
     * Adds a new patient.
     *
     * @param patientDto the DTO containing patient data
     * @return the ID of the newly created patient
     */
    @PostMapping("/add")
    public String addPatient(@RequestBody PatientDto patientDto) {
        return patientService.addPatient(patientDto);
    }

    /**
     * Updates an existing patient.
     *
     * @param id         the ID of the patient to update
     * @param patientDto the DTO containing updated patient data
     */
    @PutMapping("/{id}")
    public void updatePatient(
            @PathVariable UUID id,
            @RequestBody PatientDto patientDto
    ) {
        patientService.updatePatient(id, patientDto);
    }

    /**
     * Deletes a patient by ID.
     *
     * @param id the ID of the patient to delete
     */
    @DeleteMapping("/{id}")
    public void deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
    }
}
