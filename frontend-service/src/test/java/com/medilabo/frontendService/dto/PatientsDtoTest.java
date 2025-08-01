package com.medilabo.frontendService.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class PatientsDtoTest {

  @Test
  public void testDataGetterSetter() {
    PatientsDto dto = new PatientsDto();

    PatientsDto.Patient patient = new PatientsDto.Patient();
    UUID id = UUID.randomUUID();
    patient.setId(id);

    List<PatientsDto.Patient> list = Arrays.asList(patient);
    dto.setData(list);

    assertSame(list, dto.getData());
    assertEquals(1, dto.getData().size());
    assertEquals(id, dto.getData().get(0).getId());
  }

  @Test
  public void testPatientIsInstanceOfPatientDto() {
    PatientsDto.Patient patient = new PatientsDto.Patient();
    assertTrue(patient instanceof PatientDto);
  }
}
