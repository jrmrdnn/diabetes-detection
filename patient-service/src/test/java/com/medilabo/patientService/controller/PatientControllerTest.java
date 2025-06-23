package com.medilabo.patientService.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medilabo.patientService.dto.PatientDto;
import com.medilabo.patientService.dto.PatientsDto;
import com.medilabo.patientService.model.Patient;
import com.medilabo.patientService.service.PatientService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;

class PatientControllerTest {

  private PatientService patientService;
  private PatientController patientController;

  @BeforeEach
  void setUp() {
    patientService = mock(PatientService.class);
    patientController = new PatientController(patientService);
  }

  @ParameterizedTest
  @ValueSource(strings = { "lastName", "birthDate", "gender", "unknown" })
  void getAllPatients_shouldCallServiceWithCorrectPageRequest(String sortBy) {
    PatientsDto patientsDto = new PatientsDto();
    when(patientService.getAllPatients(any(PageRequest.class))).thenReturn(
      patientsDto
    );

    PatientsDto result = patientController.getAllPatients(1, 10, "asc", sortBy);

    ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(
      PageRequest.class
    );
    verify(patientService).getAllPatients(captor.capture());
    PageRequest pageRequest = captor.getValue();

    assertThat(pageRequest.getPageNumber()).isEqualTo(1);
    assertThat(pageRequest.getPageSize()).isEqualTo(10);
    assertThat(result).isSameAs(patientsDto);
  }

  @Test
  void getAllPatients_shouldCallServiceWithCorrectPageRequestDesc() {
    PatientsDto patientsDto = new PatientsDto();
    when(patientService.getAllPatients(any(PageRequest.class))).thenReturn(
      patientsDto
    );

    PatientsDto result = patientController.getAllPatients(
      1,
      10,
      "DESC",
      "lastName"
    );

    ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(
      PageRequest.class
    );
    verify(patientService).getAllPatients(captor.capture());
    PageRequest pageRequest = captor.getValue();

    assertThat(pageRequest.getPageNumber()).isEqualTo(1);
    assertThat(pageRequest.getPageSize()).isEqualTo(10);
    assertThat(result).isSameAs(patientsDto);
  }

  @Test
  void getPatientById_shouldReturnPatientIfFound() {
    UUID id = UUID.randomUUID();
    Patient patient = new Patient();
    when(patientService.getPatientById(id)).thenReturn(Optional.of(patient));

    Patient result = patientController.getPatientById(id);

    assertThat(result).isSameAs(patient);
  }

  @Test
  void getPatientById_shouldThrowIfNotFound() {
    UUID id = UUID.randomUUID();
    when(patientService.getPatientById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> patientController.getPatientById(id))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Patient not found");
  }

  @Test
  void searchPatients_shouldCallServiceWithCorrectParams() {
    PatientsDto patientsDto = new PatientsDto();
    when(
      patientService.searchPatients(eq("john"), any(PageRequest.class))
    ).thenReturn(patientsDto);

    PatientsDto result = patientController.searchPatients("john", 2, 7);

    ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(
      PageRequest.class
    );
    verify(patientService).searchPatients(eq("john"), captor.capture());
    PageRequest pageRequest = captor.getValue();

    assertThat(pageRequest.getPageNumber()).isEqualTo(2);
    assertThat(pageRequest.getPageSize()).isEqualTo(7);
    assertThat(result).isSameAs(patientsDto);
  }

  @Test
  void addPatient_shouldCallServiceAndReturnId() {
    PatientDto dto = new PatientDto();
    when(patientService.addPatient(dto)).thenReturn("new-id");

    String result = patientController.addPatient(dto);

    verify(patientService).addPatient(dto);
    assertThat(result).isEqualTo("new-id");
  }

  @Test
  void updatePatient_shouldCallService() {
    UUID id = UUID.randomUUID();
    PatientDto dto = new PatientDto();

    patientController.updatePatient(id, dto);

    verify(patientService).updatePatient(id, dto);
  }

  @Test
  void deletePatient_shouldCallService() {
    UUID id = UUID.randomUUID();

    patientController.deletePatient(id);

    verify(patientService).deletePatient(id);
  }
}
