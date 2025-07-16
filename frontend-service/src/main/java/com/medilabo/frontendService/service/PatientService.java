package com.medilabo.frontendService.service;

import com.medilabo.frontendService.dto.PatientDto;
import com.medilabo.frontendService.dto.PatientsDto;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
