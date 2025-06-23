package com.medilabo.patientService.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.medilabo.patientService.dto.PatientDto;
import com.medilabo.patientService.model.Gender;
import com.medilabo.patientService.model.Patient;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PatientMapperTest {

  private PatientMapper patientMapper;

  @BeforeEach
  void setUp() {
    patientMapper = Mappers.getMapper(PatientMapper.class);
  }

  @Test
  void testToDto() {
    Patient patient = new Patient();
    patient.setFirstName("John");
    patient.setLastName("Doe");
    patient.setBirthDate(LocalDate.of(1980, 1, 1));
    patient.setGender(Gender.M);
    patient.setPostalAddress("123 Main St");
    patient.setPhoneNumber("1234567890");

    PatientDto dto = patientMapper.toDto(patient);

    assertNotNull(dto);
    assertEquals("John", dto.getFirstName());
    assertEquals("Doe", dto.getLastName());
    assertEquals(LocalDate.of(1980, 1, 1), dto.getBirthDate());
    assertEquals(Gender.M, dto.getGender());
    assertEquals("123 Main St", dto.getPostalAddress());
    assertEquals("1234567890", dto.getPhoneNumber());
  }

  @Test
  void testToPatient() {
    PatientDto dto = new PatientDto();
    dto.setFirstName("Jane");
    dto.setLastName("Smith");
    dto.setBirthDate(LocalDate.of(1990, 5, 15));
    dto.setGender(Gender.F);
    dto.setPostalAddress("456 Elm St");
    dto.setPhoneNumber("0987654321");

    Patient patient = patientMapper.toPatient(dto);

    assertNotNull(patient);
    assertEquals("Jane", patient.getFirstName());
    assertEquals("Smith", patient.getLastName());
    assertEquals(LocalDate.of(1990, 5, 15), patient.getBirthDate());
    assertEquals(Gender.F, patient.getGender());
    assertEquals("456 Elm St", patient.getPostalAddress());
    assertEquals("0987654321", patient.getPhoneNumber());
  }

  @Test
  void testToDtoWithNull() {
    PatientDto dto = patientMapper.toDto(null);
    assertEquals(null, dto);
  }

  @Test
  void testToPatientWithNull() {
    Patient patient = patientMapper.toPatient(null);
    assertEquals(null, patient);
  }

  @Test
  void testToDtoWithEmptyPatient() {
    Patient patient = new Patient();
    PatientDto dto = patientMapper.toDto(patient);
    assertNotNull(dto);
    assertEquals(null, dto.getFirstName());
    assertEquals(null, dto.getLastName());
    assertEquals(null, dto.getBirthDate());
    assertEquals(null, dto.getGender());
    assertEquals(null, dto.getPostalAddress());
    assertEquals(null, dto.getPhoneNumber());
  }

  @Test
  void testToPatientWithEmptyDto() {
    PatientDto dto = new PatientDto();
    Patient patient = patientMapper.toPatient(dto);
    assertNotNull(patient);
    assertEquals(null, patient.getFirstName());
    assertEquals(null, patient.getLastName());
    assertEquals(null, patient.getBirthDate());
    assertEquals(null, patient.getGender());
    assertEquals(null, patient.getPostalAddress());
    assertEquals(null, patient.getPhoneNumber());
  }
}
