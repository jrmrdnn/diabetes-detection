package com.medilabo.assessmentService.dto;

import com.medilabo.assessmentService.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
}
