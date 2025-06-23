package com.medilabo.patientService.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medilabo.patientService.dto.PatientDto;
import com.medilabo.patientService.dto.PatientsDto;
import com.medilabo.patientService.mapper.PatientMapper;
import com.medilabo.patientService.model.Patient;
import com.medilabo.patientService.repository.PatientRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class PatientServiceTest {

  @Mock
  private PatientRepository patientRepository;

  @Mock
  private PatientMapper patientMapper;

  @InjectMocks
  private PatientService patientService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getAllPatients_shouldReturnPatientsDto() {
    List<Patient> patients = Arrays.asList(new Patient(), new Patient());
    Page<Patient> page = new PageImpl<>(patients, PageRequest.of(0, 2), 2);
    when(patientRepository.findAll(any(Pageable.class))).thenReturn(page);

    PatientsDto result = patientService.getAllPatients(PageRequest.of(0, 2));

    assertThat(result.getData()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    verify(patientRepository).findAll(any(Pageable.class));
  }

  @Test
  void getPatientById_shouldReturnPatient() {
    UUID id = UUID.randomUUID();
    Patient patient = new Patient();
    when(patientRepository.findById(id)).thenReturn(Optional.of(patient));

    Optional<Patient> result = patientService.getPatientById(id);

    assertThat(result).isPresent();
    verify(patientRepository).findById(id);
  }

  @Test
  void searchPatients_shouldReturnPatientsDto() {
    List<Patient> patients = Collections.singletonList(new Patient());
    Page<Patient> page = new PageImpl<>(patients, PageRequest.of(0, 1), 1);
    when(
      patientRepository.searchByName(eq("John"), any(Pageable.class))
    ).thenReturn(page);

    PatientsDto result = patientService.searchPatients(
      "John",
      PageRequest.of(0, 1)
    );

    assertThat(result.getData()).hasSize(1);
    verify(patientRepository).searchByName(eq("John"), any(Pageable.class));
  }

  @Test
  void addPatient_shouldSaveAndReturnId() {
    PatientDto dto = new PatientDto();
    Patient patient = new Patient();
    UUID id = UUID.randomUUID();
    patient.setId(id);

    when(patientMapper.toPatient(dto)).thenReturn(patient);
    when(patientRepository.save(patient)).thenReturn(patient);

    String result = patientService.addPatient(dto);

    assertThat(result).isEqualTo(id.toString());
    verify(patientRepository).save(patient);
  }

  @Test
  void updatePatient_shouldUpdateFieldsAndSave() {
    UUID id = UUID.randomUUID();
    PatientDto dto = new PatientDto();
    dto.setPhoneNumber("123456789");
    dto.setPostalAddress("123 Main St");
    Patient patient = new Patient();
    patient.setId(id);

    when(patientRepository.findById(id)).thenReturn(Optional.of(patient));
    when(patientRepository.save(patient)).thenReturn(patient);

    patientService.updatePatient(id, dto);

    assertThat(patient.getPhoneNumber()).isEqualTo("123456789");
    assertThat(patient.getPostalAddress()).isEqualTo("123 Main St");
    verify(patientRepository).save(patient);
  }

  @Test
  void updatePatient_shouldThrowIfNotFound() {
    UUID id = UUID.randomUUID();
    PatientDto dto = new PatientDto();
    when(patientRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> patientService.updatePatient(id, dto))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Patient not found with id");
  }

  @Test
  void deletePatient_shouldDeleteIfExists() {
    UUID id = UUID.randomUUID();
    Patient patient = new Patient();
    patient.setId(id);
    when(patientRepository.findById(id)).thenReturn(Optional.of(patient));

    patientService.deletePatient(id);

    verify(patientRepository).delete(patient);
  }

  @Test
  void deletePatient_shouldThrowIfNotFound() {
    UUID id = UUID.randomUUID();
    when(patientRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> patientService.deletePatient(id))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Patient not found with id");
  }
}
