package com.medilabo.frontendService.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.medilabo.frontendService.dto.PatientDto;
import com.medilabo.frontendService.dto.PatientsDto;

import lombok.NonNull;

/**
 * Service class for patient-related operations.
 */
@Service
public class PatientService {

    public String calculateAge(@NonNull LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        int age = today.getYear() - birthDate.getYear();
        if (today.getDayOfYear() < birthDate.getDayOfYear()) age--;
        return String.valueOf(age);
    }

    public PatientDto getPatientDto(PatientsDto.Patient patient) {
        PatientDto patientDto = new PatientDto();
        patientDto.setPostalAddress(patient.getPostalAddress());
        patientDto.setPhoneNumber(patient.getPhoneNumber());
        return patientDto;
    }
}
