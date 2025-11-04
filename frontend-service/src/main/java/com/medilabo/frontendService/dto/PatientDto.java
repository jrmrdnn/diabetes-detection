package com.medilabo.frontendService.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for Patient.
 */
@Getter
@Setter
public class PatientDto {
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate birthDate;

    @NotNull(message="Le genre est obligatoire")
    private Gender gender;

    private String postalAddress;

    @Pattern(regexp = "^$|^0[0-9]( [0-9]{2}){4}$", message = "Le numéro de téléphone doit être vide ou au format 0X XX XX XX XX")
    private String phoneNumber;
}
