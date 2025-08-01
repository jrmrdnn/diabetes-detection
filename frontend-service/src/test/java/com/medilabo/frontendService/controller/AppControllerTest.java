package com.medilabo.frontendService.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.medilabo.frontendService.dto.PatientsDto;
import com.medilabo.frontendService.feign.PatientFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
class AppControllerTest {

  @Mock
  private PatientFeignClient patientFeignClient;

  @InjectMocks
  private AppController appController;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(
      appController,
      "baseUrl",
      "http://localhost:8080"
    );
  }

  @Test
  void whenPageLessThanOne_thenRedirectToLogin() {
    Model model = new ExtendedModelMap();
    String view = appController.showApp(0, 5, "asc", "lastName", model);
    assertEquals("redirect:http://localhost:8080/login", view);
  }

  @Test
  void whenSizeInvalid_thenRedirectToLogin() {
    Model model = new ExtendedModelMap();
    String viewTooLarge = appController.showApp(
      1,
      51,
      "asc",
      "lastName",
      model
    );
    assertEquals("redirect:http://localhost:8080/login", viewTooLarge);

    String viewTooSmall = appController.showApp(1, 0, "asc", "lastName", model);
    assertEquals("redirect:http://localhost:8080/login", viewTooSmall);
  }

  @Test
  void whenTotalPagesLessThanRequestedPage_thenRedirectToApp() {
    PatientsDto patients = mock(PatientsDto.class);
    when(patients.getTotalPages()).thenReturn(1);
    when(patientFeignClient.getAllPatients(1, 5, "asc", "lastName")).thenReturn(
      patients
    );

    Model model = new ExtendedModelMap();
    String view = appController.showApp(2, 5, "asc", "lastName", model);
    assertEquals("redirect:http://localhost:8080/app", view);
  }

  @Test
  void whenSuccessful_thenAddPatientsToModelAndReturnAppView() {
    PatientsDto patients = mock(PatientsDto.class);
    when(patients.getTotalPages()).thenReturn(5);
    when(patientFeignClient.getAllPatients(0, 5, "asc", "lastName")).thenReturn(
      patients
    );

    Model model = new ExtendedModelMap();
    String view = appController.showApp(1, 5, "asc", "lastName", model);
    assertEquals("app", view);
    assertSame(patients, ((ExtendedModelMap) model).get("patients"));
  }

  @Test
  void whenFeignThrowsException_thenReturnAppWithErrorMessage() {
    when(patientFeignClient.getAllPatients(0, 5, "asc", "lastName")).thenThrow(
      new RuntimeException("feign error")
    );

    Model model = new ExtendedModelMap();
    String view = appController.showApp(1, 5, "asc", "lastName", model);
    assertEquals("app", view);
    Object errorMessage = ((ExtendedModelMap) model).get("errorMessage");
    assertNotNull(errorMessage);
    assertTrue(errorMessage.toString().contains("Une erreur s'est produite"));
  }
}
