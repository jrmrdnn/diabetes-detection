package com.medilabo.frontendService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.medilabo.frontendService.dto.PatientDto;
import com.medilabo.frontendService.dto.PatientsDto;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class PatientServiceTest {

  private final PatientService patientService = new PatientService();

  private int expectedAge(LocalDate birthDate) {
    LocalDate today = LocalDate.now();
    int age = today.getYear() - birthDate.getYear();
    if (today.getDayOfYear() < birthDate.getDayOfYear()) age--;
    return age;
  }

  @Test
  public void calculateAge_whenBirthDateIsToday_returnsZero() {
    LocalDate today = LocalDate.now();
    String actual = patientService.calculateAge(today);
    assertEquals(String.valueOf(expectedAge(today)), actual);
    assertEquals("0", actual);
  }

  @Test
  public void calculateAge_whenBirthdayAlreadyOccurred_thisYear_returnsExpected() {
    LocalDate today = LocalDate.now();
    LocalDate birthDate = today.minusYears(40).minusDays(1);
    String actual = patientService.calculateAge(birthDate);
    assertEquals(String.valueOf(expectedAge(birthDate)), actual);
  }

  @Test
  public void calculateAge_whenBirthdayNotYetOccurredThisYear_returnsExpectedMinusOne() {
    LocalDate today = LocalDate.now();
    LocalDate birthDate = today.minusYears(30).plusDays(1);
    String actual = patientService.calculateAge(birthDate);
    assertEquals(String.valueOf(expectedAge(birthDate)), actual);
  }

  @Test
  public void getPatientDto_mapsPostalAddressAndPhoneNumber() {
    PatientsDto.Patient patient = new PatientsDto.Patient();
    patient.setPostalAddress("123 Main St");
    patient.setPhoneNumber("+1-555-0000");

    PatientDto dto = patientService.getPatientDto(patient);

    assertNotNull(dto);
    assertEquals("123 Main St", dto.getPostalAddress());
    assertEquals("+1-555-0000", dto.getPhoneNumber());
  }

  @Test
  public void calculateAge_whenBirthDateIsNull_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () ->
      patientService.calculateAge(null)
    );
  }
}
