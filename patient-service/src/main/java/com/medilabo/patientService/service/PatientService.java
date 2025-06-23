package com.medilabo.patientService.service;

import com.medilabo.patientService.dto.PatientDto;
import com.medilabo.patientService.dto.PatientsDto;
import com.medilabo.patientService.mapper.PatientMapper;
import com.medilabo.patientService.model.Patient;
import com.medilabo.patientService.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing patients.
 * Provides methods to retrieve, add, update, and delete patient records.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    /**
     * Retrieves all patients with pagination.
     *
     * @param pageable the pagination information
     * @return a PatientsDto containing the paginated list of patients
     */
    public PatientsDto getAllPatients(Pageable pageable) {
        Page<Patient> patientPage = patientRepository.findAll(pageable);
        return toPatientsDto(patientPage);
    }

    /**
     * Retrieves a patient by their ID.
     *
     * @param id the ID of the patient
     * @return an Optional containing the patient if found, or empty if not found
     */
    public Optional<Patient> getPatientById(UUID id) {
        return patientRepository.findById(id);
    }

    /**
     * Searches for patients by their name.
     *
     * @param query    the name query to search for
     * @param pageable the pagination information
     * @return a PatientsDto containing the paginated list of patients matching the query
     */
    public PatientsDto searchPatients(String query, Pageable pageable) {
        Page<Patient> patientPage = patientRepository.searchByName(query, pageable);
        return toPatientsDto(patientPage);
    }

    /**
     * Adds a new patient.
     *
     * @param patientDto the DTO containing patient information
     * @return the ID of the newly added patient
     */
    public String addPatient(PatientDto patientDto) {
        Patient patient = patientMapper.toPatient(patientDto);
        patientRepository.save(patient);
        return patient.getId().toString();
    }

    /**
     * Updates an existing patient's information.
     *
     * @param id         the ID of the patient to update
     * @param patientDto the DTO containing updated patient information
     */
    public void updatePatient(UUID id, PatientDto patientDto) {
        Patient patient = getPatientById(id).orElseThrow(() ->
                new RuntimeException("Patient not found with id: " + id)
        );
        patient.setPhoneNumber(patientDto.getPhoneNumber());
        patient.setPostalAddress(patientDto.getPostalAddress());
        patientRepository.save(patient);
    }

    /**
     * Deletes a patient by their ID.
     *
     * @param id the ID of the patient to delete
     */
    public void deletePatient(UUID id) {
        Patient existingPatient = getPatientById(id).orElseThrow(() ->
                new RuntimeException("Patient not found with id: " + id)
        );
        patientRepository.delete(existingPatient);
    }

    /**
     * Converts a Page of Patient entities to a PatientsDto.
     *
     * @param patientPage the Page of Patient entities
     * @return a PatientsDto containing the paginated list of patients
     */
    private PatientsDto toPatientsDto(Page<Patient> patientPage) {
        PatientsDto patientsDto = new PatientsDto();
        patientsDto.setTotalElements(patientPage.getTotalElements());
        patientsDto.setTotalPages(patientPage.getTotalPages());
        patientsDto.setCurrentPage(patientPage.getNumber());
        patientsDto.setPageSize(patientPage.getSize());
        patientsDto.setData(patientPage.getContent());
        return patientsDto;
    }
}
