package com.medilabo.assessmentService.dto;

import java.time.LocalDate;

import com.medilabo.assessmentService.enums.Gender;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for Patient.
 */
@Getter
@Setter
public class PatientDto {
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private Gender gender;
  private String postalAddress;
  private String phoneNumber;
}
