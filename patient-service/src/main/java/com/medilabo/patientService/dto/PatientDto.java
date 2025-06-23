package com.medilabo.patientService.dto;

import com.medilabo.patientService.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String postalAddress;
    private String phoneNumber;
}
