package com.medilabo.frontendService.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientDto {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String postalAddress;
    private String phoneNumber;

    public enum Gender {
        F, // Femme
        M, // Homme
    }
}
